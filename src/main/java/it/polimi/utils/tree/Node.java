package it.polimi.utils.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Node<T>
{
    protected final List<Node<T>> children;
    protected T value;

    public Node()
    {
        children = new ArrayList<>();
    }

    public Node(T value)
    {
        this.value = value;
        children = new ArrayList<>();
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    public void addChild(Node<T> child)
    {
        children.add(child);
    }

    public void addChild(T childValue)
    {
        children.add(new Node<>(childValue));
    }

    public void removeChild(Node<T> child)
    {
        children.remove(child);
    }

    public List<Node<T>> getChildren()
    {
        return children;
    }

    public List<T> getChildrenValues()
    {
        return children.stream().map(Node::getValue).collect(Collectors.toList());
    }

    public Optional<Node<T>> findFirst(Predicate<T> predicate)
    {
        for(Node<T> child : children)
        {
            if(predicate.test(child.getValue()))return Optional.of(child);
            Optional<Node<T>> node = child.findFirst(predicate);
            if(node.isPresent())return node;
        }
        return Optional.empty();
    }

    public List<Node<T>> findAll(Predicate<T> predicate)
    {
        List<Node<T>> nodes = new ArrayList<>();
        for(Node<T> child : children)
        {
            if(predicate.test(child.getValue()))nodes.add(child);
            List<Node<T>> nodeList = child.findAll(predicate);
            nodes.addAll(nodeList);
        }
        return nodes;
    }
}
