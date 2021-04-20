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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.rendering.block.diff.BlockDiffMarker;
import org.xwiki.contrib.rendering.block.diff.BlockDiffMarkerFilter;
import org.xwiki.contrib.rendering.block.diff.EndBlock;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.Patch;
import org.xwiki.rendering.block.Block;

/**
 * Default implementation of {@link BlockDiffMarker}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultBlockDiffMarker implements BlockDiffMarker
{
    /**
     * The block parameter used to mark deleted and inserted blocks.
     */
    static final String DIFF_MARKER_PARAMETER = "data-xdom-diff";

    /**
     * Used to mark deleted blocks.
     */
    private static final String DELETED = "deleted";

    /**
     * Used to mark inserted blocks.
     */
    private static final String INSERTED = "inserted";

    @Inject
    private DiffManager diffManager;

    @Inject
    private List<BlockDiffMarkerFilter> diffMarkerFilters;

    private BlockListConverter blockListConverter = new BlockListConverter();

    @Override
    public boolean markDiff(Block left, Block right) throws DiffException
    {
        List<Block> leftList = this.blockListConverter.toList(left);
        List<Block> rightList = this.blockListConverter.toList(right);
        Patch<Block> patch = this.diffManager.diff(leftList, rightList, null).getPatch();
        if (!patch.isEmpty()) {
            this.blockListConverter.fromList(this.markPatch(leftList, patch));
            amend(left);
        }
        return !patch.isEmpty();
    }

    /**
     * Adds inserted blocks to the original list and marks both the deleted and inserted blocks accordingly using block
     * parameters.
     * <p>
     * Note that we treat all blocks <b>equally</b>, taking into account only the parent-child relationship and
     * considering that all blocks support parameters, i.e. that all blocks can be marked as deleted or inserted. This
     * means that the produced block tree might have to be amended in order to preserve its semantic and in order to be
     * able to render it properly. We do this in {@link #amend(Block)}.
     * 
     * @param list the list of blocks before the modification
     * @param patch the changes made to the given list
     * @return a list of blocks that contains unmodified, deleted and inserted blocks in the right order, with deleted
     *         and inserted blocks being marked accordingly using block parameters
     */
    private List<Block> markPatch(List<Block> list, Patch<Block> patch)
    {
        for (int i = patch.size() - 1; i >= 0; i--) {
            Delta<Block> delta = patch.get(i);
            int changeIndex = delta.getPrevious().getIndex();
            List<Block> deleted = delta.getPrevious().getElements();
            List<Block> inserted = delta.getNext().getElements();
            // Replace the previous (deleted) elements with the result of the merge between deleted and inserted.
            list.subList(changeIndex, changeIndex + deleted.size()).clear();
            list.addAll(changeIndex, merge(deleted, inserted));
        }
        return list;
    }

    /**
     * The deleted / inserted blocks can correspond to different levels in the XDOM tree so cannot display the changes
     * unless we alternate deleted and inserted blocks from the same level.
     * 
     * @param deleted the list of deleted blocks from a delta
     * @param inserted the list of inserted blocks from a delta
     * @return a list of blocks where deleted and inserted blocks are interleaved so that inserted blocks follow deleted
     *         blocks from the same level as much as possible
     */
    private List<Block> merge(List<Block> deleted, List<Block> inserted)
    {
        List<Block> result = new LinkedList<>();
        Iterator<Block> deletedIterator = deleted.iterator();
        Iterator<Block> insertedIterator = inserted.iterator();
        do {
            // Alternate deleted and inserted blocks on the same level.
            result.addAll(markDescendants(deletedIterator, DELETED));
            result.addAll(markDescendants(insertedIterator, INSERTED));
        } while (deletedIterator.hasNext() || insertedIterator.hasNext());
        return result;
    }

    /**
     * Mark the descendant blocks from the current level.
     * 
     * @param iterator the remaining blocks
     * @param marker the marker to use
     * @return the descendant blocks that have been processed
     */
    private List<Block> markDescendants(Iterator<Block> iterator, String marker)
    {
        List<Block> descendants = new LinkedList<>();
        int level = 0;
        while (iterator.hasNext()) {
            Block descendant = iterator.next();
            descendants.add(descendant);
            if (descendant instanceof EndBlock) {
                // Go back up, outside the block.
                level--;
                if (level < 0) {
                    if (DELETED.equals(marker)) {
                        descendants.remove(descendants.size() - 1);
                    }
                    // We stepped outside the root.
                    break;
                }
            } else {
                // Mark only the blocks on the first level (the blocks below inherit the marker).
                if (level == 0) {
                    descendant.setParameter(DIFF_MARKER_PARAMETER, marker);
                }
                // Go down, inside the block.
                level++;
            }
        }
        return descendants;
    }

    /**
     * The tree of blocks obtained after marking the deleted and inserted blocks may have to be amended in order to
     * preserve its semantic and in order to be rendered properly. The following changes are required:
     * <ul>
     * <li>some blocks can't be rendered with parameters so they have to be wrapped in another block; this is the case
     * with Word, Space, SpecialSymbol and NewLine blocks that have to be wrapped in a Format block for instance</li>
     * <li>keeping both the deleted and inserted children might require some grouping in order to maintain the parent
     * valid; inserted blocks can "duplicate" deleted blocks and the parent might not allow it</li>
     * </ul>
     * 
     * @param markedBlock a block that has directly (on itself) or indirectly (on its descendants) change markers
     *            (inserted or deleted)
     */
    private void amend(Block markedBlock)
    {
        this.diffMarkerFilters.forEach(filter -> filter.filter(markedBlock));
    }
}
