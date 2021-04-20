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
import org.xwiki.diff.DiffException;
import org.xwiki.diff.Patch;
import org.xwiki.diff.PatchException;
import org.xwiki.rendering.block.Block;
import org.xwiki.stability.Unstable;

/**
 * Computes the changes between two {@link Block}s of content.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface BlockDiffManager
{
    /**
     * Computes the changes between two blocks, taking into account:
     * <ul>
     * <li>block parameters, see {@link Block#getParameters()}</li>
     * <li>block data (custom fields, specific to each block type)</li>
     * <li>child blocks, see {@link Block#getChildren()}.</li>
     * </ul>
     * 
     * @param previous the block before the modification
     * @param next the block after the modification
     * @return a patch that can transform previous into next
     * @throws DiffException if computing the changes fails
     */
    Patch<Block> diff(Block previous, Block next) throws DiffException;

    /**
     * Applies the given patch to the specified block.
     * 
     * @param previous the block before the modification
     * @param patch the changes to apply
     * @return the block after the modification
     * @throws PatchException if it fails to apply the patch
     */
    Block apply(Block previous, Patch<Block> patch) throws PatchException;

    /**
     * Restores the given patch on the specified block.
     * 
     * @param next the block after the modification
     * @param patch the changes to restore
     * @return the block before the modification
     * @throws PatchException if it fails to restore the patch
     */
    Block restore(Block next, Patch<Block> patch) throws PatchException;
}
