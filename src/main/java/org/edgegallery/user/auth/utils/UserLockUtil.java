/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.user.auth.utils;

import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserLockUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLockUtil.class);

    // when login failed 5 times, account will be locked.
    private static final Set<RequestLimitRule> REQUEST_LIMIT_RULES = Collections
        .singleton(RequestLimitRule.of(Duration.ofMinutes(5), 4));

    // locked overtime
    private static final long OVERTIME = 5 * 60 * 1000L;

    private static final RequestRateLimiter LIMITER = new InMemorySlidingWindowRequestRateLimiter(REQUEST_LIMIT_RULES);

    private static final Map<String, Long> LOCKED_USERS_MAP = new HashMap<>();

    /**
     * judge whether the user is locked.
     *
     * @param userFlag user flag(user name or email or telephone)
     * @return is the user locked
     */
    public boolean isLocked(String userFlag) {
        if (!LOCKED_USERS_MAP.containsKey(userFlag)) {
            return false;
        }

        long lockedTime = LOCKED_USERS_MAP.get(userFlag);
        if (System.currentTimeMillis() - lockedTime < OVERTIME) {
            return true;
        } else {
            LOCKED_USERS_MAP.remove(userFlag);
            return false;
        }
    }

    /**
     * add failed count when login failed.
     *
     * @param userFlag user flag(user name or email or telephone)
     */
    public void addFailedCount(String userFlag) {
        boolean isOverLimit = LIMITER.overLimitWhenIncremented(userFlag);
        if (isOverLimit) {
            LOCKED_USERS_MAP.put(userFlag, System.currentTimeMillis());
        }
    }

    /**
     * clear failed count when login success.
     *
     * @param userFlag user flag(user name or email or telephone)
     */
    public void clearFailedCount(String userFlag) {
        LIMITER.resetLimit(userFlag);
    }
}
