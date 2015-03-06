package de.gesellix.docker.client

import org.codehaus.groovy.runtime.MethodClosure
import spock.lang.Specification

class LowLevelDockerClientSpec extends Specification {

  def "dockerBaseUrl should default to http://localhost:2375"() {
    def client = new LowLevelDockerClient()
    expect:
    client.dockerBaseUrl?.toString() == new URL("http://127.0.0.1:2375").toString()
  }

  def "dockerBaseUrl should support tcp protocol"() {
    def client = new LowLevelDockerClient(dockerHost: "tcp://127.0.0.1:2375")
    expect:
    client.dockerBaseUrl?.toString() == new URL("http://127.0.0.1:2375").toString()
  }

  def "dockerBaseUrl should support tls port"() {
    def client = new LowLevelDockerClient(dockerHost: "tcp://127.0.0.1:2376")
    def tmpDockerCertPath = File.createTempDir()
    given:
    def oldDockerCertPath = System.setProperty("docker.cert.path", tmpDockerCertPath.absolutePath)
    expect:
    client.dockerBaseUrl?.toString() == new URL("https://127.0.0.1:2376").toString()
    cleanup:
    if (oldDockerCertPath) {
      System.setProperty("docker.cert.path", oldDockerCertPath)
    }
    else {
      System.clearProperty("docker.cert.path")
    }
    tmpDockerCertPath.delete()
  }

  def "dockerBaseUrl should support https protocol"() {
    def client = new LowLevelDockerClient(dockerHost: "https://127.0.0.1:2376")
    expect:
    client.dockerBaseUrl?.toString() == new URL("https://127.0.0.1:2376").toString()
  }

  def "generic request with bad config: #requestConfig"() {
    def client = new LowLevelDockerClient(dockerHost: "https://127.0.0.1:2376")
    when:
    client.request(requestConfig)
    then:
    def ex = thrown(RuntimeException)
    ex.message == "bad request config"
    where:
    requestConfig << [null, [], [:], ["foo": "bar"]]
  }

  def "#method request with bad config: #requestConfig"() {
    def client = new LowLevelDockerClient(dockerHost: "https://127.0.0.1:2376")
    when:
    new MethodClosure(client, method).call(requestConfig)
    then:
    def ex = thrown(RuntimeException)
    ex.message == "bad request config"
    where:
    requestConfig  | method
    null           | "get"
    null           | "post"
    []             | "get"
    []             | "post"
    [:]            | "get"
    [:]            | "post"
    ["foo": "bar"] | "get"
    ["foo": "bar"] | "post"
  }

  def "get request uses the GET method"() {
    def client = new LowLevelDockerClient(dockerHost: "https://127.0.0.1:2376")
    given:
    client.metaClass.request = { config ->
      config.method
    }
    when:
    def method = client.get("/foo")
    then:
    method == "GET"
  }

  def "post request uses the POST method"() {
    def client = new LowLevelDockerClient(dockerHost: "https://127.0.0.1:2376")
    given:
    client.metaClass.request = { config ->
      config.method
    }
    when:
    def method = client.post("/foo")
    then:
    method == "POST"
  }
}
