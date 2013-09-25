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
public interface DotGraphAttributes {

    String _BOUNDS_ATTR = "-bounds";

    String _SHAPE_ATTR = "-shape";

    String BUILD_FILE_ATTR = "build.file";

    String DESCRIPTION_ATTR = "description";

    String DRAW2DFGCOLOR_ATTR = "draw2dfgcolor";

    String DRAW2DFILLCOLOR_ATTR = "draw2dfillcolor";

    String DRAW2DLINEWIDTH_ATTR = "draw2dlinewidth";

    String IF_CONDITION_ATTR = "ifCondition";

    String LABEL_ATTR = "label";

    String MINHEIGHT_ATTR = "minheight";

    String MINWIDTH_ATTR = "minwidth";

    double PATH_ITERATOR_FLATNESS = 1.0;

    String LINK_PARAMETERS_ATTR = "link.params";

    String LINK_SUBANT_DIRECTORIES = "link.subant.directories";

    String LINK_TASK_ATTR = "link.task";

    String POSITION_ATTR = "pos";

    String SHAPE_ATTR = "shape";

    String UNLESS_CONDITION_ATTR = "unlessCondition";

}
