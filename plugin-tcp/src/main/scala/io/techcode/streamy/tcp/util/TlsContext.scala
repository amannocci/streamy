/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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

import java.security.KeyStore
import java.security.cert.CertPathValidatorException
import java.util

import akka.actor.ActorSystem
import akka.stream.{TLSClientAuth, TLSProtocol}
import com.typesafe.sslconfig.ssl._
import com.typesafe.sslconfig.util.{LoggerFactory, NoDepsLogger}
import javax.net.ssl.{SSLContext, TrustManagerFactory, X509TrustManager}
import org.slf4j.{ILoggerFactory, LoggerFactory => SLoggerFactory}

object TlsContext {

  private val logger = SLoggerFactory.getLogger(getClass.getName)
  private val loggerFactory: LoggerFactory = new AkkaLoggerFactory(SLoggerFactory.getILoggerFactory)

  /**
    * Create a ssl/tls context.
    *
    * @param system implicit actor system.
    * @return tls context based on configuration.
    */
  def newSSLContext()(implicit system: ActorSystem): SSLContext = {
    // Extract configuration from akka ssl configuration
    val akkaOverrides = system.settings.config.getConfig("akka.ssl-config")
    val defaults = system.settings.config.getConfig("ssl-config")
    val sslConfig = SSLConfigFactory.parse(akkaOverrides.withFallback(defaults))

    // Initial ssl context
    val sslContext = if (sslConfig.default) {
      validateDefaultTrustManager(sslConfig)
      SSLContext.getDefault
    } else {
      // Break out the static methods as much as we can...
      val keyManagerFactory = buildKeyManagerFactory(sslConfig)
      val trustManagerFactory = buildTrustManagerFactory(sslConfig)
      new ConfigSSLContextBuilder(loggerFactory, sslConfig, keyManagerFactory, trustManagerFactory).build()
    }

    // Protocols
    val defaultParams = sslContext.getDefaultSSLParameters
    val defaultProtocols = defaultParams.getProtocols
    val protocols = configureProtocols(defaultProtocols, sslConfig)
    defaultParams.setProtocols(protocols)

    // Ciphers
    val defaultCiphers = defaultParams.getCipherSuites
    val cipherSuites = configureCipherSuites(defaultCiphers, sslConfig)
    defaultParams.setCipherSuites(cipherSuites)

    // SSL context
    sslContext
  }

  /**
    * Build key manager factory based on ssl config.
    *
    * @param ssl ssl config.
    * @return key manager factory.
    */
  private def buildKeyManagerFactory(ssl: SSLConfigSettings): KeyManagerFactoryWrapper =
    new DefaultKeyManagerFactoryWrapper(ssl.keyManagerConfig.algorithm)

  /**
    * Build trust manager factory based on ssl config.
    *
    * @param ssl ssl config.
    * @return trust manager factory.
    */
  private def buildTrustManagerFactory(ssl: SSLConfigSettings): TrustManagerFactoryWrapper =
    new DefaultTrustManagerFactoryWrapper(ssl.trustManagerConfig.algorithm)

  /**
    * Configure protocols.
    *
    * @param existingProtocols selected protocols.
    * @param sslConfig         ssl configuration.
    * @return list of protocols.
    */
  private def configureProtocols(existingProtocols: Array[String], sslConfig: SSLConfigSettings): Array[String] = {
    val definedProtocols = sslConfig.enabledProtocols match {
      case Some(configuredProtocols) =>
        // If we are given a specific list of protocols, then return it in exactly that order,
        // assuming that it's actually possible in the SSL context.
        configuredProtocols.filter(existingProtocols.contains).toArray
      case None =>
        // Otherwise, we return the default protocols in the given list.
        Protocols.recommendedProtocols.filter(existingProtocols.contains)
    }

    if (!sslConfig.loose.allowWeakProtocols) {
      val deprecatedProtocols = Protocols.deprecatedProtocols
      for (deprecatedProtocol <- deprecatedProtocols) {
        if (definedProtocols.contains(deprecatedProtocol)) {
          throw new IllegalStateException(s"Weak protocol $deprecatedProtocol found in ssl-config.protocols!")
        }
      }
    }
    definedProtocols
  }

  /**
    * Configure cipher suites.
    *
    * @param existingCiphers selected ciphers.
    * @param sslConfig       ssl confuration.
    * @return list of ciphers.
    */
  private def configureCipherSuites(existingCiphers: Array[String], sslConfig: SSLConfigSettings): Array[String] = {
    val definedCiphers = sslConfig.enabledCipherSuites match {
      case Some(configuredCiphers) =>
        // If we are given a specific list of ciphers, return it in that order.
        configuredCiphers.filter(existingCiphers.contains(_)).toArray

      case None =>
        Ciphers.recommendedCiphers.filter(existingCiphers.contains(_)).toArray
    }

    if (!sslConfig.loose.allowWeakCiphers) {
      val deprecatedCiphers = Ciphers.deprecatedCiphers
      for (deprecatedCipher <- deprecatedCiphers) {
        if (definedCiphers.contains(deprecatedCipher)) {
          throw new IllegalStateException(s"Weak cipher $deprecatedCipher found in ssl-config.ciphers!")
        }
      }
    }
    definedCiphers
  }

  /**
    * Validate default trust manager based on ssl configuration.
    *
    * @param sslConfig ssl configuration.
    */
  private def validateDefaultTrustManager(sslConfig: SSLConfigSettings): Unit = {
    // If we are using a default SSL context, we can't filter out certificates with weak algorithms
    // We ALSO don't have access to the trust manager from the SSLContext without doing horrible things
    // with reflection.
    //
    // However, given that the default SSLContextImpl will call out to the TrustManagerFactory and any
    // configuration with system properties will also apply with the factory, we can use the factory
    // method to recreate the trust manager and validate the trust certificates that way.
    //
    // This is really a last ditch attempt to satisfy https://wiki.mozilla.org/CA:MD5and1024 on root certificates.
    //
    // http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/sun/security/ssl/SSLContextImpl.java#79

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(null.asInstanceOf[KeyStore])
    val trustManager: X509TrustManager = tmf.getTrustManagers()(0).asInstanceOf[X509TrustManager]

    val constraints = sslConfig.disabledKeyAlgorithms
      .map(a => AlgorithmConstraintsParser.parseAll(AlgorithmConstraintsParser.expression, a).get)
      .toSet
    val algorithmChecker = new AlgorithmChecker(loggerFactory, Set(), constraints)
    for (cert <- trustManager.getAcceptedIssuers) {
      try {
        algorithmChecker.checkKeyAlgorithms(cert)
      } catch {
        case e: CertPathValidatorException =>
          logger.warn(
            "You are using ssl-config.default=true and have a weak certificate in your default trust store! (You can modify akka.ssl-config.disabledKeyAlgorithms to remove this message.)",
            e
          )
      }
    }
  }

}

/**
  * Akka logger factory implementation.
  */
private class AkkaLoggerFactory(lf: ILoggerFactory = SLoggerFactory.getILoggerFactory) extends LoggerFactory {

  def createLogger(name: String): NoDepsLogger = {
    new NoDepsLogger {
      private val logger = lf.getLogger(name)

      def warn(msg: String): Unit = logger.warn(msg)

      def isDebugEnabled: Boolean = logger.isDebugEnabled

      def error(msg: String): Unit = logger.error(msg)

      def error(msg: String, throwable: Throwable): Unit = logger.error(msg, throwable)

      def debug(msg: String): Unit = logger.debug(msg)

      def info(msg: String): Unit = logger.info(msg)
    }
  }

  def apply(clazz: Class[_]): NoDepsLogger = createLogger(clazz.getName)

  def apply(name: String): NoDepsLogger = createLogger(name)

}
