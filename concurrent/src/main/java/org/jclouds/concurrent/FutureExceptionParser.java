/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 */
package org.jclouds.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;

/**
 * Transforms the result of a future as soon as it is available.
 * 
 * @author Adrian Cole
 */
public class FutureExceptionParser<T> implements Future<T> {

   private final Future<T> delegate;
   private final Function<Exception, T> function;

   public FutureExceptionParser(Future<T> delegate, Function<Exception, T> function) {
      this.delegate = delegate;
      this.function = function;
   }

   public boolean cancel(boolean mayInterruptIfRunning) {
      return delegate.cancel(mayInterruptIfRunning);
   }

   public T get() throws InterruptedException, ExecutionException {
      try {
         return delegate.get();
      } catch (ExecutionException e) {
         return attemptConvert(e);
      }
   }

   private T attemptConvert(ExecutionException e) throws ExecutionException {
      if (e.getCause() instanceof Exception) {
         T returnVal = function.apply((Exception) e.getCause());
         if (returnVal != null)
            return returnVal;
      }
      throw e;
   }

   public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
      try {
         return delegate.get(timeout, unit);
      } catch (ExecutionException e) {
         return attemptConvert(e);
      }
   }

   public boolean isCancelled() {
      return delegate.isCancelled();
   }

   public boolean isDone() {
      return delegate.isDone();
   }

}