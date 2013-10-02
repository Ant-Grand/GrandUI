// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2004, Christophe Labouisse All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ggtools.grand.ui.widgets.property;

import java.util.Map.Entry;

/**
 * @author Christophe Labouisse
 */
class PropertyPair {
    /**
     * Field name.
     */
    private String name;

    /**
     * Field value.
     */
    private String value;

    /**
     * Constructor for PropertyPair.
     * @param name Object
     * @param value Object
     */
    public PropertyPair(final Object name, final Object value) {
        this.name = (name instanceof String) ? (String) name : name.toString();
        this.value = (value instanceof String) ? (String) value : value.toString();
    }

    /**
     * Creates a new PropertyPair from a Map.Entry. The entry key & value should
     * be both instance of String.
     *
     * @param entry Entry<Object, Object>
     */
    public PropertyPair(final Entry<Object, Object> entry) {
        this(entry.getKey(), entry.getValue());
    }

    /**
     * Method getName.
     * @return String
     */
    public final String getName() {
        return name;
    }

    /**
     * Method setName.
     * @param name String
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Method getValue.
     * @return String
     */
    public final String getValue() {
        return value;
    }

    /**
     * Method setValue.
     * @param value String
     */
    public final void setValue(final String value) {
        this.value = value;
    }

}
