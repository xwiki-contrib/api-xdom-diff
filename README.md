# XDOM Diff API

Provides an API to compute and render the changes between two [XDOM trees](https://rendering.xwiki.org). The high-level algorithm implemented is this:

1. the input XDOM trees are _serialized_ as two **lists** of blocks; it's important that the serialization function is reversible, meaning that we need to be able to recompute the XDOM trees from the two lists of blocks; for this we introduce an ``EndBlock`` to mark where the descendants of a block end in the list of Blocks; you can view this ``EndBlock`` like a closing tag in a markup language such as HTML.
2. we compute the changes between the two lists of blocks using a standard diff algorithm (such as the Myers diff algorithm); this produces a patch
3. we _merge_ the patch with the first list of blocks (the list that corresponds to the first XDOM tree) this way:
    1. the _unmodified_ blocks are _copied_ as is
    2. the _deleted_ blocks (from the patch) are kept in the list but are marked by setting their ``data-xdom-diff`` parameter to ``deleted``
    3. the _inserted_ blocks (from the patch) are inserted in the list and are marked by setting their ``data-xdom-diff`` parameter to ``inserted``
    4. the lists of _deleted_ and _inserted_ blocks of each delta from the patch are _mixed_ (interleaved) based on their level in the XDOM tree, making sure that deleted blocks are followed by inserted blocks at the same level (parent)
4. the result of the previous step (a list of blocks with deleted and inserted blocks marked accordingly using the ``data-xdom-diff`` parameter) is _parsed_ as an XDOM tree, the diff tree
5. rendering the diff XDOM tree as is won't show the changes because not all blocks render their parameters (all blocks support parameters but not all parameters are rendered); in order to fix this we need to **filter** the diff XDOM tree and transform it so that the changes are properly rendered (e.g. wrap word blocks in a format block); this is done by implementing multiple ``BlockDiffMarkerFilter``s.
6. the filtered diff XDOM tree is rendered using a renderer that can output custom parameters (such as ``data-xdom-diff``); we used the XHTML 1.0 renderer in our tests

See [``DefaultBlockDiffMarkerTest``](https://github.com/xwiki-contrib/api-xdom-diff/blob/main/src/test/java/org/xwiki/contrib/rendering/internal/block/diff/DefaultBlockDiffMarkerTest.java) for an example of how to use the API.

**NOTE**: This API is not stable and has not been tested well enough. It's more a proof of concept. The remaining work is to add more ``BlockDiffMarkerFilter``s and more tests.
