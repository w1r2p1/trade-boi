/*
 * Copyright (C) 2016 An Honest Effort LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.trading.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.anhonesteffort.trading.TradeBoiConfig;
import org.anhonesteffort.trading.http.response.GetAccountsCallback;
import org.anhonesteffort.trading.http.response.PostOrderCallback;
import org.anhonesteffort.trading.http.response.ResponseCallback;
import org.anhonesteffort.trading.http.request.model.PostOrderRequest;
import org.anhonesteffort.trading.http.request.RequestSigner;
import org.anhonesteffort.trading.http.response.GetOrderBookCallback;
import org.anhonesteffort.trading.http.response.model.GetAccountsResponse;
import org.anhonesteffort.trading.http.response.model.GetOrderBookResponse;

import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpClientWrapper implements Closeable {

  private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

  private static final String PROD_API_BASE     = "https://api.gdax.com";
  private static final String SANDBOX_API_BASE  = "https://api-public.sandbox.gdax.com";
  private static final String API_PATH_BOOK     = "/products/BTC-USD/book?level=3";
  private static final String API_PATH_ORDERS   = "/orders";
  private static final String API_PATH_ACCOUNTS = "/accounts";

  private final OkHttpClient  client   = HttpClient.getInstance();
  private final ObjectReader  reader   = new ObjectMapper().reader();
  private final ObjectWriter  writer   = new ObjectMapper().writer();
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final RequestSigner signer;
  private final String        API_BASE;

  public HttpClientWrapper(TradeBoiConfig config) throws NoSuchAlgorithmException {
    API_BASE = config.getGdaxSandbox() ? SANDBOX_API_BASE : PROD_API_BASE;
    signer   = new RequestSigner(
        config.getGdaxAccessKey(), config.getGdaxSecretKey(), config.getGdaxPassword()
    );
  }

  private boolean setExceptionIfShutdown(CompletableFuture<?> future) {
    if (shutdown.get()) {
      future.completeExceptionally(new HttpException("this http client wrapper is shutdown"));
      return true;
    } else {
      return false;
    }
  }

  public CompletableFuture<GetOrderBookResponse> geOrderBook() {
    CompletableFuture<GetOrderBookResponse> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      client.newCall(new Request.Builder().url(
          API_BASE + API_PATH_BOOK
      ).build()).enqueue(new GetOrderBookCallback(reader, future));
    }

    return future;
  }

  public CompletableFuture<GetAccountsResponse> getAccounts() throws IOException {
    CompletableFuture<GetAccountsResponse> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ACCOUNTS).get();
      signer.sign(request, "GET", API_PATH_ACCOUNTS, Optional.empty());
      client.newCall(request.build()).enqueue(new GetAccountsCallback(reader, future));
    }

    return future;
  }

  public CompletableFuture<Boolean> postOrder(PostOrderRequest order) throws IOException {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      RequestBody     body    = RequestBody.create(TYPE_JSON, writer.writeValueAsString(order));
      Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ORDERS).post(body);
      signer.sign(request, "POST", API_PATH_ORDERS, Optional.of(body));
      client.newCall(request.build()).enqueue(new PostOrderCallback(reader, future));
    }

    return future;
  }

  public CompletableFuture<Response> cancelOrder(String orderId) throws IOException {
    CompletableFuture<Response> future  = new CompletableFuture<>();
    String                      path    = API_PATH_ORDERS + "/" + orderId;
    Request.Builder             request = new Request.Builder().url(API_BASE + path).delete();

    signer.sign(request, "DELETE", path, Optional.empty());
    client.newCall(request.build()).enqueue(new ResponseCallback(future));

    return future;
  }

  public CompletableFuture<Response> cancelAllOrders() throws IOException {
    CompletableFuture<Response> future  = new CompletableFuture<>();
    Request.Builder             request = new Request.Builder().url(API_BASE + API_PATH_ORDERS).delete();

    signer.sign(request, "DELETE", API_PATH_ORDERS, Optional.empty());
    client.newCall(request.build()).enqueue(new ResponseCallback(future));

    return future;
  }

  @Override
  public void close() {
    shutdown.set(true);
  }

}
