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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.rendering.block.AbstractBlock;
import org.xwiki.rendering.block.Block;
import org.xwiki.stability.Unstable;

/**
 * Marks the end of a block in the list representation of a block. You can view this as the equivalent of a closing tag
 * in a mark-up language like XML. This end block is needed in order to be able to recompute the block from its list
 * representation.
 *
 * @version $Id$
 * @since 1.0
 */
@Unstable
public class EndBlock extends AbstractBlock
{
    private final Class<?> type;

    /**
     * Creates a new instance to mark the end of the given block.
     * 
     * @param block the block to end
     */
    public EndBlock(Block block)
    {
        this.type = block.getClass();
    }

    /**
     * @return the block type
     */
    public Class<?> getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof EndBlock && super.equals(obj)) {
            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getType(), ((EndBlock) obj).getType());

            return builder.isEquals();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());
        builder.append(getType());

        return builder.toHashCode();
    }
}
