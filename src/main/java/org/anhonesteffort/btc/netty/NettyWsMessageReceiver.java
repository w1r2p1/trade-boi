/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
//The MIT License
//
//Copyright (c) 2009 Carl Bystršm
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package org.anhonesteffort.btc.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.anhonesteffort.btc.ws.WsException;
import org.anhonesteffort.btc.ws.WsMessageSorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class NettyWsMessageReceiver extends SimpleChannelInboundHandler<Object> {

  private static final Logger log       = LoggerFactory.getLogger(NettyWsMessageReceiver.class);
  private static final String SUBSCRIBE = "{ \"type\": \"subscribe\", \"product_id\": \"BTC-USD\" }";

  private final ObjectReader reader = new ObjectMapper().reader();
  private final WebSocketClientHandshaker shake;
  private final WsMessageSorter sorter;

  public NettyWsMessageReceiver(URI uri, WsMessageSorter sorter) {
    this.sorter = sorter;
    shake       = WebSocketClientHandshakerFactory.newHandshaker(
        uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
    );
  }

  @Override
  public void channelActive(ChannelHandlerContext context) {
    log.info("connection opened");
    shake.handshake(context.channel());
  }

  private WebSocketFrame getWsFrameOrThrow(Object msg) throws WsException {
    if (msg instanceof TextWebSocketFrame  ||
        msg instanceof CloseWebSocketFrame ||
        msg instanceof PongWebSocketFrame)
    {
      return (WebSocketFrame) msg;
    } else {
      throw new WsException("unknown message type -> " + msg.getClass().getSimpleName());
    }
  }

  @Override
  public void messageReceived(ChannelHandlerContext context, Object msg)
      throws WsException, IOException, InterruptedException, ExecutionException
  {
    if (!shake.isHandshakeComplete()) { // todo: calls out to a volatile boolean D;
      shake.finishHandshake(context.channel(), (FullHttpResponse) msg);
      context.writeAndFlush(new TextWebSocketFrame(SUBSCRIBE));
      log.info("handshake completed");
      return;
    }

    WebSocketFrame frame = getWsFrameOrThrow(msg);

    if (frame instanceof TextWebSocketFrame) {
      sorter.sort(reader.readTree(((TextWebSocketFrame) frame).text()), System.nanoTime());
    } else if (frame instanceof CloseWebSocketFrame) {
      CloseWebSocketFrame close = (CloseWebSocketFrame) frame;
      throw new WsException(
          "socket closed with code " + close.statusCode() +
              " and reason -> " + close.reasonText()
      );
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
    log.error("error in receive pipeline", cause);
    context.close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    log.error("socket closed");
    context.close();
  }

}
