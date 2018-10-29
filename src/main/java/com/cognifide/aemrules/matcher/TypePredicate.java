/*-
 * #%L
 * AEM Rules for SonarQube
 * %%
 * Copyright (C) 2015 Cognifide Limited
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.cognifide.aemrules.matcher;

import java.util.function.Predicate;
import org.sonar.plugins.java.api.semantic.Type;

@FunctionalInterface
public interface TypePredicate extends Predicate<Type> {

    String ANY_TYPE = "ANY_TYPE";

    static TypePredicate is(String fullyQualifiedTypeName) {
        if (ANY_TYPE.equals(fullyQualifiedTypeName)) {
            return type -> true;
        } else {
            return type -> type.is(fullyQualifiedTypeName);
        }
    }

}