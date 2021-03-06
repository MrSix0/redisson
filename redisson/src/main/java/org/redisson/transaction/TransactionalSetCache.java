/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.transaction;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.redisson.RedissonSetCache;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RSetCache;
import org.redisson.client.RedisClient;
import org.redisson.client.protocol.decoder.ListScanResult;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.operation.TransactionalOperation;
import org.redisson.transaction.operation.set.AddCacheOperation;
import org.redisson.transaction.operation.set.MoveOperation;
import org.redisson.transaction.operation.set.RemoveCacheOperation;

/**
 * 
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class TransactionalSetCache<V> extends BaseTransactionalSet<V> {

    private final RSetCache<V> set;
    
    public TransactionalSetCache(CommandAsyncExecutor commandExecutor, long timeout, List<TransactionalOperation> operations,
            RSetCache<V> set) {
        super(commandExecutor, timeout, operations, set);
        this.set = set;
    }

    @Override
    protected ListScanResult<Object> scanIteratorSource(String name, RedisClient client, long startPos,
            String pattern) {
        return ((RedissonSetCache<?>)set).scanIterator(name, client, startPos, pattern);
    }

    @Override
    protected RFuture<Set<V>> readAllAsyncSource() {
        return set.readAllAsync();
    }
    
    public RFuture<Boolean> addAsync(V value, long ttl, TimeUnit ttlUnit) {
        return addAsync(value, new AddCacheOperation(set, value, ttl, ttlUnit));
    }
    
    @Override
    protected TransactionalOperation createAddOperation(final V value) {
        return new AddCacheOperation(set, value);
    }
    
    @Override
    protected MoveOperation createMoveOperation(final String destination, final V value, final long threadId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected RLock getLock(V value) {
        return set.getLock(value);
    }
    
    @Override
    protected TransactionalOperation createRemoveOperation(final Object value) {
        return new RemoveCacheOperation(set, value);
    }
    
}
