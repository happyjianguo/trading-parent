package com.dili.trading.aspect;

import com.dili.ss.redis.service.RedisUtil;
import com.dili.trading.annotation.ForbidDuplicateCommit;
import com.dili.trading.common.CacheKey;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 防重请求拦截切面
 *
 * @Auther: miaoguoxin
 * @Date: 2020/7/7 10:24
 */
@Component
@Aspect
public class DuplicateCommitAspect {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private HttpServletRequest request;

    private static final String DUPLICATE_TOKEN_HEADER = "duplicate_commit_token";


    /**
     * 切入点
     *
     * @author miaoguoxin
     * @date 2020/7/7
     */
    @Pointcut("@annotation(com.dili.trading.annotation.ForbidDuplicateCommit)")
    public void logPointCut() {
    }

    /**
     * 环绕通知
     *
     * @author miaoguoxin
     * @date 2020/7/7
     */
    @Around("@annotation(forbid)&&logPointCut()")
    public Object around(ProceedingJoinPoint pjp, ForbidDuplicateCommit forbid) throws Throwable {
        if (forbid == null) {
            return pjp.proceed();
        }
        String header = request.getHeader(DUPLICATE_TOKEN_HEADER);
        if (StringUtils.isBlank(header)) {
            throw new RuntimeException("非法请求");
        }
        String key = CacheKey.FORBID_DUPLICATE_TOKEN_PREFIX + header;
        try {
            long increment = redisUtil.increment(key, 1L);
            //只有返回2L说明这个token在redis是已存在的，并且没有被用过，为合法请求
            //返回其他数字说明token非法，如返回1L说明该token在redis中不存在
            //返回大于2L说明该token已被使用
            if (2L == increment) {
                return pjp.proceed();
            }
            throw new RuntimeException("token过期或不存在，请刷新后重试");
        } finally {
            redisUtil.remove(key);
        }
    }

}
