package com.ruoyi.framework.aspectj;

import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.redis.LocalCache;
import com.ruoyi.common.exception.ServiceException;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalRateLimiterTest
{
    @Test
    void rejectsRequestsAboveTheLimitWithoutRedis() throws Throwable
    {
        LocalCache cache = new LocalCache();
        RateLimiterAspect aspect = new RateLimiterAspect();
        ReflectionTestUtils.setField(aspect, "redisCache", cache);
        Method method = Endpoint.class.getDeclaredMethod("limited");
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        JoinPoint point = mock(JoinPoint.class);
        when(point.getSignature()).thenReturn(signature);
        when(point.getTarget()).thenReturn(new Endpoint());
        RateLimiter annotation = method.getAnnotation(RateLimiter.class);

        aspect.doBefore(point, annotation);
        aspect.doBefore(point, annotation);
        assertThrows(ServiceException.class, () -> aspect.doBefore(point, annotation));
        cache.deleteObject(aspect.getCombineKey(annotation, point));
        assertDoesNotThrow(() -> aspect.doBefore(point, annotation));
    }

    static class Endpoint
    {
        @RateLimiter(count = 2, time = 60)
        void limited()
        {
        }
    }
}
