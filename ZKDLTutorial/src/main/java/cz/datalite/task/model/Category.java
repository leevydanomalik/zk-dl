/**
 * Copyright 19.3.11 (c) DataLite, spol. s r.o. All rights reserved.
 * Web: http://www.datalite.cz    Mail: info@datalite.cz 
 */
package cz.datalite.task.model;

import cz.datalite.helpers.EqualsHelper;
import cz.datalite.helpers.JpaEqualHelper;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Entity mapped to a database table.
 *
 * @author Jiri Bubnik
 */
@Entity
public class Category implements Serializable {
    /** Primary key (autogenerated). */
    @Id
    @GeneratedValue
    private Long idCategory;

    /** Name of the category */
    @NotNull
    private String name;

    /** Description of the category */
    private String description;

    /**
     * ************** Serializable / Equals / Hash / toString ****************
     */

    private static final long serialVersionUID = 31012010L;

    @Override
    public boolean equals(Object obj) {
        return this == obj || (JpaEqualHelper.isEntitySameClass(this, obj) &&
                EqualsHelper.isEquals(this.idCategory, ((Category) obj).getIdCategory()));
    }

    @Override
    public int hashCode() {
        return this.idCategory == null ? super.hashCode() : this.idCategory.hashCode();
    }

    @Override
    public String toString() {
        return "cz.datalite.task.model.Category[id=" + this.idCategory + "][val=" + name + "]";
    }

    /**
     * ************************ SETTERS & GETTERS ***************************
     */

    public Long getIdCategory() {
        return idCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}