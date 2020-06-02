package it.polimi.utils.file;

import it.polimi.gd.beans.DirectoryMetadata;
import it.polimi.utils.tree.Node;
import it.polimi.utils.tree.Tree;

import java.util.Comparator;
import java.util.List;

public class DirectoriesTree extends Tree<DirectoryMetadata>
{

    private DirectoriesTree()
    {
        super(new DirectoryMetadata(0, "/", null, 0));
    }

    public static DirectoriesTree build(List<DirectoryMetadata> directories)
    {
        DirectoriesTree tree = new DirectoriesTree();
        directories.sort(Comparator.comparingInt(DirectoryMetadata::getParentId));
        directories.forEach(directory ->
        {
            if(directory.getParentId() == 0) tree.root.addChild(directory);
            else
            {
                Node<DirectoryMetadata> dirNode = tree.root.findFirst(dir -> dir.getId() == directory.getParentId()).orElseThrow(() -> new IllegalStateException("The parent of "+directory.getName()+" directory, doesn't exist!"));
                dirNode.addChild(directory);
            }
        });
        return tree;
    }
}
