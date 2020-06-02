package it.polimi.utils.tree;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Tree<T>
{
    protected final Node<T> root;

    public Tree()
    {
        this.root = new Node<>();
    }

    public Tree(Node<T> root)
    {
        this.root = root;
    }

    public Tree(T rootValue)
    {
        this.root = new Node<>(rootValue);
    }

    public Node<T> getRoot()
    {
        return root;
    }

    public Optional<Node<T>> findFirst(Predicate<T> predicate)
    {
        return root.findFirst(predicate);
    }

    public List<Node<T>> findAll(Predicate<T> predicate)
    {
        return root.findAll(predicate);
    }

}
