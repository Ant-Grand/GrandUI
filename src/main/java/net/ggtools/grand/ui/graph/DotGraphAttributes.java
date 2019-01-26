// $Id$
/*
 * ====================================================================
 * Copyright (c) 2002-2003, Christophe Labouisse All rights reserved.
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

package net.ggtools.grand.ui.graph;

/**
 * An interface holding the constants for some useful attributes use in the dot
 * &amp; draw2d graph.
 *
 * @author Christophe Labouisse
 */
public interface DotGraphAttributes {

    /**
     * Field _BOUNDS_ATTR.
     * (value is {@value #_BOUNDS_ATTR})
     */
    String _BOUNDS_ATTR = "-bounds";

    /**
     * Field _SHAPE_ATTR.
     * (value is {@value #_SHAPE_ATTR})
     */
    String _SHAPE_ATTR = "-shape";

    /**
     * Field BUILD_FILE_ATTR.
     * (value is {@value #BUILD_FILE_ATTR})
     */
    String BUILD_FILE_ATTR = "build.file";

    /**
     * Field DESCRIPTION_ATTR.
     * (value is {@value #DESCRIPTION_ATTR})
     */
    String DESCRIPTION_ATTR = "description";

    /**
     * Field DRAW2DFGCOLOR_ATTR.
     * (value is {@value #DRAW2DFGCOLOR_ATTR})
     */
    String DRAW2DFGCOLOR_ATTR = "draw2dfgcolor";

    /**
     * Field DRAW2DFILLCOLOR_ATTR.
     * (value is {@value #DRAW2DFILLCOLOR_ATTR})
     */
    String DRAW2DFILLCOLOR_ATTR = "draw2dfillcolor";

    /**
     * Field DRAW2DLINEWIDTH_ATTR.
     * (value is {@value #DRAW2DLINEWIDTH_ATTR})
     */
    String DRAW2DLINEWIDTH_ATTR = "draw2dlinewidth";

    /**
     * Field IF_CONDITION_ATTR.
     * (value is {@value #IF_CONDITION_ATTR})
     */
    String IF_CONDITION_ATTR = "ifCondition";

    /**
     * Field LABEL_ATTR.
     * (value is {@value #LABEL_ATTR})
     */
    String LABEL_ATTR = "label";

    /**
     * Field MINHEIGHT_ATTR.
     * (value is {@value #MINHEIGHT_ATTR})
     */
    String MINHEIGHT_ATTR = "minheight";

    /**
     * Field MINWIDTH_ATTR.
     * (value is {@value #MINWIDTH_ATTR})
     */
    String MINWIDTH_ATTR = "minwidth";

    /**
     * Field PATH_ITERATOR_FLATNESS.
     * (value is {@value #PATH_ITERATOR_FLATNESS})
     */
    double PATH_ITERATOR_FLATNESS = 1.0;

    /**
     * Field LINK_PARAMETERS_ATTR.
     * (value is {@value #LINK_PARAMETERS_ATTR})
     */
    String LINK_PARAMETERS_ATTR = "link.params";

    /**
     * Field LINK_SUBANT_DIRECTORIES.
     * (value is {@value #LINK_SUBANT_DIRECTORIES})
     */
    String LINK_SUBANT_DIRECTORIES = "link.subant.directories";

    /**
     * Field LINK_TASK_ATTR.
     * (value is {@value #LINK_TASK_ATTR})
     */
    String LINK_TASK_ATTR = "link.task";

    /**
     * Field POSITION_ATTR.
     * (value is {@value #POSITION_ATTR})
     */
    String POSITION_ATTR = "pos";

    /**
     * Field SHAPE_ATTR.
     * (value is {@value #SHAPE_ATTR})
     */
    String SHAPE_ATTR = "shape";

    /**
     * Field UNLESS_CONDITION_ATTR.
     * (value is {@value #UNLESS_CONDITION_ATTR})
     */
    String UNLESS_CONDITION_ATTR = "unlessCondition";

}
