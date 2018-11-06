/*-
 * #%L
 * AEM Rules for SonarQube
 * %%
 * Copyright (C) 2015-2018 Cognifide Limited
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
package com.cognifide.aemrules.htl.checks;

import com.cognifide.aemrules.htl.api.ParsingErrorRule;
import com.cognifide.aemrules.metadata.Metadata;
import com.cognifide.aemrules.tag.Tags;
import com.cognifide.aemrules.version.AemVersion;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.sling.scripting.sightly.compiler.expression.Expression;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.html.node.Attribute;
import org.sonar.plugins.html.node.TagNode;

@Rule(
    key = NamingAndReusingConditionsCheck.RULE_KEY,
    name = NamingAndReusingConditionsCheck.RULE_MESSAGE,
    priority = Priority.INFO,
    tags = Tags.AEM
)
@AemVersion(
    all = true
)
@Metadata(
    technicalDebt = "10min"
)
@ParsingErrorRule
public class NamingAndReusingConditionsCheck extends AbstractHtlCheck {

    static final String RULE_KEY = "HTL-5";

    static final String RULE_MESSAGE = "Consider caching data-sly-test conditions";

    private static final String SLY_TEST = "data-sly-test";

    private Set<String> unnamedConditions = new HashSet<>();

    private Set<String> namedConditions = new HashSet<>();

    @Override
    public void startHtlElement(List<Expression> expressions, TagNode node) {
        if (isConditionReused(expressions, node)) {
            createViolation(node.getStartLinePosition(), RULE_MESSAGE);
        }
        updateConditionSets(expressions, node);
    }

    private boolean isConditionReused(List<Expression> expressions, TagNode node) {
        String condition = expressions.stream()
            .map(Expression::getRawText)
            .map(text -> text.replaceAll("[${}]", ""))
            .findFirst()
            .orElse("");

        return unnamedConditions.stream()
            .anyMatch(condition::equals) && // To see if someone is reusing not cached condition
            node.getAttributes().stream()
                .map(Attribute::getName)
                .anyMatch(name -> name.equals(SLY_TEST)); // To see if someone is not declaring new cached condition
    }

    private void updateConditionSets(List<Expression> expressions, TagNode node) {
        Optional<String> condition = node.getAttributes().stream()
            .map(Attribute::getName)
            .filter(text -> text.contains(SLY_TEST))
            .findFirst();
        if (condition.isPresent() && !condition.get().equals(SLY_TEST)) {
            condition = Optional.of(condition.get().substring(14));
            namedConditions.add(condition.get());
        } else {
            unnamedConditions.addAll(expressions.stream()
                .map(Expression::getRawText)
                .map(text -> text.replaceAll("[${}]", ""))
                .filter(text -> !namedConditions.contains(text))
                .collect(Collectors.toSet()));
        }
    }
}