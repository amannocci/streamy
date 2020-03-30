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

import com.dimafeng.testcontainers.{Container, ForAllTestContainer, GenericContainer, MultipleContainers}
import io.techcode.streamy.TestSystem


trait TcpSpec extends TestSystem with ForAllTestContainer {

  val ncatPlain: GenericContainer = GenericContainer(
    dockerImage = "itsthenetwork/alpine-ncat",
    exposedPorts = Seq(5000),
    command = Seq("-lk", "5000")
  )

  val ncatTls: GenericContainer = GenericContainer(
    dockerImage = "itsthenetwork/alpine-ncat",
    exposedPorts = Seq(5000),
    command = Seq("-lk", "5000", "-i", "3s", "--ssl", "--ssl-cert", "/tmp/test-cert.pem", "--ssl-key", "/tmp/test-key.pem")
  ).configure { c =>
    c.withFileSystemBind(
      getClass.getClassLoader.getResource("test-cert.pem").getFile,
      "/tmp/test-cert.pem"
    )
    c.withFileSystemBind(
      getClass.getClassLoader.getResource("test-key.pem").getFile,
      "/tmp/test-key.pem"
    )
  }

  override val container: MultipleContainers = MultipleContainers(ncatPlain, ncatTls)

}
