/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.techcode.streamy.tcp.util

import akka.actor.ActorSystem
import akka.stream.{TLSClientAuth, TLSProtocol}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import com.typesafe.sslconfig.akka.util.AkkaLoggerFactory
import com.typesafe.sslconfig.ssl.ConfigSSLContextBuilder
import javax.net.ssl.SSLContext

case class TlsContext(
  sslContext: SSLContext,
  negotiateNewSession: TLSProtocol.NegotiateNewSession
)

object TlsContext {

  /**
    * Create a tls context.
    *
    * @param system implicit actor system.
    * @return tls context based on configuration.
    */
  def create()(implicit system: ActorSystem): TlsContext = {
    // Extract configuration from akka ssl configuration
    val sslConfig = AkkaSSLConfig(system)
    val settings = sslConfig.config

    // Akka logger factory
    val mkLogger = new AkkaLoggerFactory(system)

    // Initial ssl context
    val sslContext = if (sslConfig.config.default) {
      sslConfig.validateDefaultTrustManager(settings)
      SSLContext.getDefault
    } else {
      // Break out the static methods as much as we can...
      val keyManagerFactory = sslConfig.buildKeyManagerFactory(settings)
      val trustManagerFactory = sslConfig.buildTrustManagerFactory(settings)
      new ConfigSSLContextBuilder(mkLogger, settings, keyManagerFactory, trustManagerFactory).build()
    }

    // Protocols
    val defaultParams = sslContext.getDefaultSSLParameters
    val defaultProtocols = defaultParams.getProtocols
    val protocols = sslConfig.configureProtocols(defaultProtocols, settings)
    defaultParams.setProtocols(protocols)

    // Ciphers
    val defaultCiphers = defaultParams.getCipherSuites
    val cipherSuites = sslConfig.configureCipherSuites(defaultCiphers, settings)
    defaultParams.setCipherSuites(cipherSuites)

    // Auth
    import com.typesafe.sslconfig.ssl.{ClientAuth => SslClientAuth}
    val clientAuth = settings.sslParametersConfig.clientAuth match {
      case SslClientAuth.Default => None
      case SslClientAuth.Want => Some(TLSClientAuth.Want)
      case SslClientAuth.Need => Some(TLSClientAuth.Need)
      case SslClientAuth.None => Some(TLSClientAuth.None)
    }

    // New session negotiation
    val negotiateNewSession = TLSProtocol.NegotiateNewSession(
      Some(cipherSuites.toList),
      Some(protocols.toList),
      clientAuth,
      Some(defaultParams)
    )

    TlsContext(sslContext, negotiateNewSession)
  }

}
