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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.rendering.block.diff.BlockDiffManager;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.Patch;
import org.xwiki.diff.PatchException;
import org.xwiki.rendering.block.Block;

/**
 * Default implementation of {@link BlockDiffManager}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultBlockDiffManager implements BlockDiffManager
{
    @Inject
    private DiffManager diffManager;

    private BlockListConverter blockListConverter = new BlockListConverter();

    @Override
    public Patch<Block> diff(Block previous, Block next) throws DiffException
    {
        List<Block> previousList = this.blockListConverter.toList(previous);
        List<Block> nextList = this.blockListConverter.toList(next);
        return this.diffManager.diff(previousList, nextList, null).getPatch();
    }

    @Override
    public Block apply(Block previous, Patch<Block> patch) throws PatchException
    {
        return this.blockListConverter.fromList(patch.apply(this.blockListConverter.toList(previous)));
    }

    @Override
    public Block restore(Block next, Patch<Block> patch) throws PatchException
    {
        return this.blockListConverter.fromList(patch.restore(this.blockListConverter.toList(next)));
    }
}
