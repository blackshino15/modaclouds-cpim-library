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
package it.polimi.modaclouds.cpimlibrary.entitymng.statements;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/**
 * Represents a DELETE statement.
 *
 * @author Fabio Arcidiacono.
 * @see it.polimi.modaclouds.cpimlibrary.entitymng.statements.Statement
 */
@Data
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DeleteStatement extends Statement {

    /* (non-Javadoc)
     *
     * @see it.polimi.modaclouds.cpimlibrary.entitymng.statements.Statement#addField(String, Object)
     */
    @Override
    public void addField(String name, Object value) {
        // DELETE statements does not support field lists
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (!haveConditions()) {
            return String.format("DELETE FROM %s", this.getTable());
        } else {
            StringBuilder conditions = new StringBuilder();
            Iterator entries = getConditionsIterator();
            while (entries.hasNext()) {
                Object next = entries.next();
                conditions.append(next.toString());
                if (entries.hasNext()) {
                    conditions.append(" ");
                }
            }
            return String.format("DELETE FROM %s WHERE %s", this.getTable(), conditions);
        }
    }
}