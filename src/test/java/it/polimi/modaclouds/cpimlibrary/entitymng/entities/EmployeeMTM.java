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
package it.polimi.modaclouds.cpimlibrary.entitymng.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@ToString(exclude = "projects")
@EqualsAndHashCode(exclude = "projects")
@NoArgsConstructor
@Entity
@Table(name = "EmployeeMTM", schema = "gae-test@pu")
public class EmployeeMTM {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "EMPLOYEE_ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SALARY")
    private Long salary;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "EMPLOYEE_PROJECT", joinColumns = {@JoinColumn(name = "EMPLOYEE_ID")}, inverseJoinColumns = {@JoinColumn(name = "PROJECT_ID")})
    private List<ProjectMTM> projects;

    public void addProjects(ProjectMTM... projects) {
        if (this.projects == null) {
            this.projects = new ArrayList<ProjectMTM>();
        }
        Collections.addAll(this.projects, projects);
    }
}
