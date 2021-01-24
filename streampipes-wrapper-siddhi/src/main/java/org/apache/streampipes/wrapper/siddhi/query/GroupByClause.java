/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.wrapper.siddhi.query;

import org.apache.streampipes.wrapper.siddhi.constants.SiddhiConstants;
import org.apache.streampipes.wrapper.siddhi.query.expression.Expression;
import org.apache.streampipes.wrapper.siddhi.query.expression.PropertyExpression;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GroupByClause extends Expression {

  private List<PropertyExpression> propertyExpressions;

  public static GroupByClause create(List<PropertyExpression> groupByProperties) {
    return new GroupByClause(groupByProperties);
  }

  public static GroupByClause create(PropertyExpression... outputProperties) {
    return new GroupByClause(Arrays.asList(outputProperties));
  }

  private GroupByClause(List<PropertyExpression> groupByProperties) {
    this.propertyExpressions = groupByProperties;
  }

  @Override
  public String toSiddhiEpl() {
    return join(SiddhiConstants.WHITESPACE, "group by", join(SiddhiConstants.COMMA,
            propertyExpressions
                    .stream()
                    .map(PropertyExpression::toSiddhiEpl)
                    .collect(Collectors.toList())));
  }

}
