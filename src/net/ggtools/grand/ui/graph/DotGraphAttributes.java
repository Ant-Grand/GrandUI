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
 * An interface holding the constants for some useful attributes use in the dot &
 * draw2d graph.
 * 
 * @author Christophe Labouisse
 */
interface DotGraphAttributes {

    static final String _BOUNDS_ATTR = "-bounds";

    static final String _SHAPE_ATTR = "-shape";

    static final String DESCRIPTION_ATTR = "description";

    static final String DRAW2DFGCOLOR_ATTR = "draw2dfgcolor";

    static final String DRAW2DFILLCOLOR_ATTR = "draw2dfillcolor";

    static final String DRAW2DLINEWIDTH_ATTR = "draw2dlinewidth";
    
    static final String IF_CONDITION_ATTR = "ifCondition";

    static final String LABEL_ATTR = "label";

    static final String MINHEIGHT_ATTR = "minheight";

    static final String MINWIDTH_ATTR = "minwidth";

    static final double PATH_ITERATOR_FLATNESS = 1.0;

    static final String POSITION_ATTR = "pos";

    static final String SHAPE_ATTR = "shape";

    static final String UNLESS_CONDITION_ATTR = "unlessCondition";


}