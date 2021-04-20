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
package org.xwiki.contrib.rendering.internal.block.diff;

import java.util.Objects;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.rendering.block.diff.BlockDiffMarkerFilter;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.Block.Axes;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;

import static org.xwiki.contrib.rendering.internal.block.diff.DefaultBlockDiffMarker.DIFF_MARKER_PARAMETER;

/**
 * Wraps modified word and space blocks with a format block in order to be able to render the changes.
 * 
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("formatWrapper")
@Singleton
public class FormatWrapperFilter implements BlockDiffMarkerFilter
{
    @Override
    public void filter(Block root)
    {
        root.getBlocks(block -> (block instanceof WordBlock || block instanceof SpaceBlock)
            && block.getParameter(DIFF_MARKER_PARAMETER) != null, Axes.DESCENDANT_OR_SELF).forEach(this::wrapBlock);
    }

    private void wrapBlock(Block block)
    {
        Block previousSibling = block.getPreviousSibling();
        if (previousSibling instanceof FormatBlock && Objects
            .equals(previousSibling.getParameter(DIFF_MARKER_PARAMETER), block.getParameter(DIFF_MARKER_PARAMETER))) {
            // Adding the block to the previous sibling doesn't remove it from its current parent..
            block.getParent().removeBlock(block);
            previousSibling.addChild(block);
        } else {
            FormatBlock formatBlock = new FormatBlock();
            formatBlock.setParameter(DIFF_MARKER_PARAMETER, block.getParameter(DIFF_MARKER_PARAMETER));
            block.getParent().replaceChild(formatBlock, block);
            formatBlock.addChild(block);
        }
    }
}
