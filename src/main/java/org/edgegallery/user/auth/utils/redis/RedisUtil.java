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

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import javax.annotation.PostConstruct;
import org.edgegallery.user.auth.config.RedisConfig;
import org.edgegallery.user.auth.exception.UserAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    private RedisUtil() {
    }

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

    /**
     * save verification code.
     */
    public static void saveVerificationCode(String key, String value) {

        try (StatefulRedisConnection<String, String> connection = RedisPoolUtil.getConnection()) {
            RedisCommands<String, String> commands = connection.sync();
            commands.set(key, value);
            commands.expire(key, redisConfig.getVerificationTimeout());
        } catch (UserAuthException e) {
            LOGGER.error("failed to connect redis.");
        }
    }

    /**
     * get value by key.
     *
     * @return
     */
    public static String get(String key) {
        try (StatefulRedisConnection<String, String> connection = RedisPoolUtil.getConnection()) {
            return connection.sync().get(key);
        } catch (UserAuthException e) {
            LOGGER.error("failed to connect redis.");
        }
        return null;

    }

    /**
     * delete key and value.
     */
    public static void delete(String key) {
        try (StatefulRedisConnection<String, String> connection = RedisPoolUtil.getConnection()) {
            connection.sync().del(key);
        } catch (UserAuthException e) {
            LOGGER.error("failed to connect redis.");
        }
    }

}