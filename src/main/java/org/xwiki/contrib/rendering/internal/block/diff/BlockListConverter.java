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

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.xwiki.contrib.rendering.block.diff.EndBlock;
import org.xwiki.diff.PatchException;
import org.xwiki.rendering.block.Block;

/**
 * Utility class to serialize an XDOM as a list of blocks and to parse it back.
 *
 * @version $Id$
 * @since 1.0
 */
public class BlockListConverter
{
    /**
     * Serializes an XDOM as a list of blocks in a way that can be reverted with {@link #fromList(List)}.
     * 
     * @param root the XDOM root
     * @return the list of blocks from the given XDOM
     */
    public List<Block> toList(Block root)
    {
        List<Block> list = new LinkedList<>();
        if (root != null) {
            list.add(root);
            for (Block child : root.getChildren()) {
                list.addAll(toList(child));
            }
            // Clear the list of children. It will be restored when calling #fromList().
            root.getChildren().clear();
            list.add(new EndBlock(root));
        }
        return list;
    }

    /**
     * Rebuilds an XDOM tree from a list of blocks.
     * 
     * @param list a list of blocks that was returned by {@link #toList(Block)}
     * @return the restored XDOM tree
     * @throws PatchException if the XDOM cannot be restored from the given list of blocks
     */
    public Block fromList(List<Block> list) throws PatchException
    {
        Stack<Block> stack = new Stack<>();
        int blockCount = 0;
        for (Block block : list) {
            blockCount++;
            if (block instanceof EndBlock) {
                // The end block must have a matching start block.
                if (stack.isEmpty()) {
                    throw new PatchException("End block doesn't have a matching start block.");
                }
                EndBlock endBlock = (EndBlock) block;
                Block startBlock = stack.pop();
                if (startBlock.getClass() != endBlock.getType()) {
                    throw new PatchException("End block doesn't match the start block.");
                }
                if (!stack.isEmpty()) {
                    Block parent = stack.peek();
                    parent.addChild(startBlock);
                } else if (blockCount == list.size()) {
                    // We processed all blocks.
                    return startBlock;
                } else {
                    throw new PatchException("Block list cannot be converted into a single tree.");
                }
            } else {
                // Clear the list of child blocks. We'll add the child blocks one by one afterwards.
                block.getChildren().clear();
                stack.push(block);
            }
        }

        if (!stack.isEmpty()) {
            throw new PatchException("Start block doesn't have a matching end block.");
        } else {
            // The input list was empty.
            return null;
        }
    }
}
