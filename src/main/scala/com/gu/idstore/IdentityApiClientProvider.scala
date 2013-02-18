package com.gu.idstore

import org.apache.commons.httpclient.params.HttpConnectionManagerParams
import org.apache.commons.httpclient.{HttpClient, MultiThreadedHttpConnectionManager}
import com.gu.identity.client.IdentityApiClient
import com.google.inject.Singleton


@Singleton
class IdentityApiClientProvider {
  val httpClient = {
    val params = new HttpConnectionManagerParams
    params.setMaxTotalConnections(20)
    params.setDefaultMaxConnectionsPerHost(20)
    params.setSoTimeout(10000)
    params.setConnectionTimeout(1000)

    val connectionManager = new MultiThreadedHttpConnectionManager
    connectionManager.setParams(params)

    val client = new HttpClient(connectionManager)
    client.getParams.setConnectionManagerTimeout(1)
    client
  }
  val idPublicKey = "MIIDOjCCAi0GByqGSM44BAEwggIgAoIBAQD4gSsAjnc7RF4liv95iS+h/LWkaRdqgVwwt8lk46UxD25SNn9w82uosczhDP2645F6ppGoewKQAO3CvXLqjRrJiV28RRSStOZhUbSgjfZbl8gg1XMHvTQRS1KtK5jgsGWpxUXxEgvPYOglsgB7XWo+OmWxntWGWgHeKjoxgg7bDe9fJHz6zblUqBu92i0+M/X5MWGrseuVn6AKkaKzrlYt5wHuzeVn07z09qjtFEPhI882pZ6o9je7nuKylZ+bOCvXRR49bZDb1te7evIK4twWPcRlh8jB6jJL/DsQWjtjvweyZD6PIGV0KxQBtxXQfG67jDzjcXuBxU4mw7VePqPhAhUAv4GmHccfqMVu+J8mXjcXGFj87hMCggEAIuPY5tVgZ8yWlHRMCBRyb9LL5OXxDw4mJXNqs1ykQ+BGY3oBoyFKuLWiEjjfLB10WHrso1iDi3ELfokvPsOKw8EEf18NactJuxrmyTRKObizoJG2Pekpwd/HoVRNJEBgCwrBZk6NouieFrzqnxlZ83gMvwa1iYOoKHNxBBU+8NRs8uSsjzocWhfnaX0x+62RyTfohvq42z6Anwzx7wlR45jNlu/4QCWWoJCUpPOawzSd545MfH0VYX5q4QT2KfxV3KB8y/3St6xocJDgtgX3Sb51tcMJU3710U+82iL6cxQkw5a/GYew7X8atUyLMaqHCX7ol0w6iMzxbeMGEM+fWAOCAQUAAoIBAAyTZaS/n9vhES/BLJicQmzqjfTO50OBJ/EKwxdoAgtrtHvVEfSIUSvYFnxdnk1znBvCzA+11xczsotyYx3BYd58Lpfncgz0Tk+q0aYNv9xt6K1fH2Xbab3TK7LIctMbIfCmr8gW7uaaOjrjuQUtkCHAS2/NwX+Vsh8fsgHEkcYaFf6hkU8+QtmZImTMUrQTq8MIblEQlk8UEEhcA3H/xUvsr9K6EZ6RuM9g9w64dXBvPto7sDYObjBC7uYufHviTZ7o2e3M9RS1GF4RJ+hR7r2TXiIAXZ79OXNdui8P+xPjKMJBRQR2fhLaCTgvhYmqDuERD0bKCttDM6zditsdjNs="
  def getIdentityApiClient = new IdentityApiClient("https://id.guardianapis.com", httpClient, idPublicKey)
}
