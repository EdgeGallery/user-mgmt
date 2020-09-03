/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.user.auth.utils.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import javax.annotation.PostConstruct;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.edgegallery.user.auth.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisPoolUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisPoolUtil.class);

    @Autowired
    private RedisConfig redisConfigWrapper;

    private static RedisConfig redisConfig;

    /**
     * init redis config.
     */
    @PostConstruct
    public void init() {
        if (redisConfig == null) {
            redisConfig = this.redisConfigWrapper;
        }
    }

    private static GenericObjectPool<StatefulRedisConnection<String, String>> getPoolInstance() {
        return RedisPoolUtilHandler.instance;
    }

    /**
     * statefull redis connection.
     *
     * @return
     */
    public static StatefulRedisConnection<String, String> getConnection() throws Exception {
        return getPoolInstance().borrowObject();
    }

    static class RedisPoolUtilHandler {

        static GenericObjectPool<StatefulRedisConnection<String, String>> instance;

        private RedisPoolUtilHandler() {
        }

        static {
            RedisURI redisUri = RedisURI.builder().withHost(redisConfig.getIp()).withPort(redisConfig.getPort())
                .withPassword(redisConfig.getPassword()).build();
            GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(redisConfig.getMaxTotal());
            poolConfig.setMaxIdle(redisConfig.getMaxIdle());
            poolConfig.setMaxWaitMillis(redisConfig.getMaxWaitMillis());
            poolConfig.setTestOnBorrow(true);
            RedisClient redisClient = RedisClient.create(redisUri);
            instance = ConnectionPoolSupport.createGenericObjectPool(redisClient::connect, poolConfig);
        }
    }
}
