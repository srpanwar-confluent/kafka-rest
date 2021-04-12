/*
 * Copyright 2021 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.kafkarest.response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Provider;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ChunkedOutput;

public class ListenableChunkedOutput<T> extends ChunkedOutput<T> {
  private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<Listener>();

  public ListenableChunkedOutput() {}

  public ListenableChunkedOutput(Type chunkType) {
    super(chunkType);
  }

  public ListenableChunkedOutput(byte[] chunkDelimiter) {
    super(chunkDelimiter);
  }

  public ListenableChunkedOutput(
      byte[] chunkDelimiter, Provider<AsyncContext> asyncContextProvider) {
    super(chunkDelimiter, asyncContextProvider);
  }

  public ListenableChunkedOutput(Type chunkType, byte[] chunkDelimiter) {
    super(chunkType, chunkDelimiter);
  }

  public ListenableChunkedOutput(String chunkDelimiter) {
    super(chunkDelimiter);
  }

  public ListenableChunkedOutput(Type chunkType, String chunkDelimiter) {
    super(chunkType, chunkDelimiter);
  }

  /**
   * Adds a listener for close event.
   * @param listener {@link Listener}
   */
  public void onClose(Listener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void close() throws IOException {
    Throwable failure = null;
    try {
      super.close();
    } catch (Throwable ex) {
      failure = ex;
      throw ex;
    } finally {
      for (Listener listener : listeners) {
        listener.onClose(failure);
      }
    }
  }

  public static interface Listener {
    void onClose(Throwable throwable);
  }
}
