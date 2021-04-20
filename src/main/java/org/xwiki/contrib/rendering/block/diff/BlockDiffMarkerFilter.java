/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.rendering.block.diff;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.Block;
import org.xwiki.stability.Unstable;

/**
 * Used to filter an XDOM with diff markers (deleted and inserted blocks) so that it can be properly rendered. This is
 * needed because {@link BlockDiffMarker} is using block parameters to mark deleted and inserted blocks but this is not
 * enough to be able to render the XDOM diff because block parameters are not always rendered.
 * 
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface BlockDiffMarkerFilter
{
    /**
     * Modify the given block so that diff markers can be properly rendered.
     * 
     * @param block the block (with diff markers) that needs to be filtered
     */
    void filter(Block block);
}
