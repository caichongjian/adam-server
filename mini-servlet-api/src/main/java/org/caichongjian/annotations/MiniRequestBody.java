/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.caichongjian.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 我本来想在adam-server直接使用Spring MVC的，但遗憾地发现实现整个jakarta.servlet-api工作量实在是太大了。
 * 我又不喜欢Servlet的形式，所以我学习参考了Spring的创意。
 * 注解内的方法的数量比起spring-web来少了很多。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 *
 * I originally wanted to use Spring MVC directly on adam-server, but unfortunately found that implementing the entire jakarta.servlet-api workload was too great.
 * I don't like the form of Servlet, so I learned from the ideas of Spring.
 * The number of methods in annotations is much less than in spring-web.
 * If you think adam-server is helpful to you, please go to the link below and click ★Star
 *
 * @author Arjen Poutsma
 *
 * @see Maven: org.springframework:spring-web:5.2.5.RELEASE  org.springframework.web.bind.annotation.RequestBody
 * @see https://github.com/spring-projects/spring-framework
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MiniRequestBody {
}
