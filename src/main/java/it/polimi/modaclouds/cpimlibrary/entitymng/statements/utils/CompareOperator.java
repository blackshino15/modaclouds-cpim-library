/**
 * Copyright 2013 deib-polimi
 * Contact: deib-polimi <marco.miglierina@polimi.it>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package it.polimi.modaclouds.cpimlibrary.entitymng.statements.utils;

/**
 * @author Fabio Arcidiacono.
 */
public enum CompareOperator {
    EQUAL("="),
    LOWER_THAN("<"),
    GREATER_THAN(">"),
    LOWER_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">="),
    NOT_EQUAL("<>");

    private String string;

    private CompareOperator(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return this.string;
    }

    public static CompareOperator fromString(String string) {
        for (CompareOperator o : values()) {
            if (o.toString().equalsIgnoreCase(string)) {
                return o;
            }
        }
        throw new IllegalArgumentException();
    }
}
