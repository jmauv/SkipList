// Jonathan Mauvais
// COP 3503, Fall 2022
// NID: jo659538

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

class Node<AnyType>
{
	private AnyType data;
	private int height;
	private List<Node<AnyType>> next = new ArrayList<>();

	Node(int height)
	{
		// with arraylists you can just add null references like so
		// to simulate multi level nodes
		for (int i = 0; i < height; i++)
			next.add(null);

		this.height = height;
	}

	Node(AnyType data, int height)
	{
		// just use exact same code as previous method for the height
		this(height);
		this.data = data;
	}

	public AnyType value()
	{
		return this.data;
	}

	public int height()
	{
		return this.height;
	}

	public Node<AnyType> next(int level)
	{
		// check for out of bounds levels
		if (level < 0 || level > (this.height - 1)) return null;

		return this.next.get(level);
	}

	public void setNext(int level, Node<AnyType> node)
	{
		this.next.set(level, node);
	}

	public void grow()
	{
		this.next.add(null);
		this.height++;
	}

	public boolean maybeGrow()
	{
		// Math.random() generates a double between 0.0 and 1.0
		// use that to simulate coinflip odds
		if (Math.random() > 0.5)
		{
			grow();
			return true;
		}

		return false;

	}

	public void trim(int height)
	{
		// starting from new height, trim up by setting references null
		// and setting node height equal to new height
		for (int i = height; i < height(); ++i)
			this.next.set(i, null);

		this.height = height;
	}
}

public class SkipList<AnyType extends Comparable<AnyType>>
{
	private int height;
	private int size;
	private Node<AnyType> head;

	SkipList()
	{
		head = new Node<>(1);
		this.height = 1;
		this.size = 0;
	}

	SkipList(int height)
	{
		this.size = 0;

		if (height < 1)
		{
			head = new Node<>(1);
			this.height = 1;
		}
		else
		{
			head = new Node<>(height);
			this.height = height;
		}
	}

	public int size()
	{
		return this.size;
	}

	public int height()
	{
		return this.height;
	}

	public Node<AnyType> head()
	{
		return this.head;
	}

	public void insert(AnyType data)
	{
		Node<AnyType> temp = head();

		// if inserting node increases max height, grow the entire list
		this.size++;
		if (getMaxHeight(size()) > height()) growSkipList();

		// generate height for new node
		int level = generateRandomHeight(height()) - 1;
		Node<AnyType> newNode = new Node<>(data, level + 1);

		// starting from top of head node, traverse through list
		for (int i = height() - 1; i >= 0; i--)
		{
			while(temp.next(i) != null)
			{
				// if the next node is less that the value we're inserting,
				// go to that node
				if (temp.next(i).value().compareTo(data) < 0)
					temp = temp.next(i);
				// else break out of this loop to level down
				else break;
			}

			// when at the new nodes level, start linking up the references
			if (i <= level)
			{
				newNode.setNext(i, temp.next(i));
				temp.setNext(i, newNode);
			}
		}
	}

	// same as previous insert method, but no need to generate height
	public void insert(AnyType data, int height)
	{
		Node<AnyType> temp = head();

		this.size++;
		if (getMaxHeight(size()) > height()) growSkipList();


		Node<AnyType> newNode = new Node<>(data, height);

		int level = height - 1;
		for (int i = height() - 1; i >= 0; i--)
		{
			while(temp.next(i) != null)
			{
				if (temp.next(i).value().compareTo(data) < 0)
					temp = temp.next(i);
				else break;
			}

			if (i <= level)
			{
				newNode.setNext(i, temp.next(i));
				temp.setNext(i, newNode);
			}
		}
	}

	public void delete(AnyType data)
	{
		Node<AnyType> temp = head();
		Node<AnyType> temp2 = head();
		int nodeHeight = 0;


		for (int i = height() - 1; i >= 0; i--)
		{
			while(temp.next(i) != null)
			{
				// for delete, to deal with dupes you have to reach the bottom
				// level of the list no matter what to make sure you'll always
				// delete the first occurence
				if (temp.next(i).value().compareTo(data) == 0 && i == 0)
				{
					// if the next node is taller than the node before it, we
					// use 2 separate loops to relink nodes
					if (temp.next(i).height() > temp.height())
					{
						// this loop is for deleting upper half
						for (int j = temp.next(i).height() - 1; j >= temp.height(); j--)
						{
							while (temp2.next(j).value().compareTo(data) != 0)
								temp2 = temp2.next(j);

							temp2.setNext(j, temp2.next(j).next(j));
						}

						// second loop is for deleting lower half, covered by
						// the node before the one being deleted
						nodeHeight = temp.height();
						for (int j = 0; j < nodeHeight; j++)
							temp.setNext(j, temp.next(j).next(j));
					}
					// if next node is equal in height or shorter than the node before,
					// can just use one loop working back up in levels to
					// completely delete the node
					else
					{
						nodeHeight = temp.next(i).height();
						for (int j = 0; j < nodeHeight; j++)
							temp.setNext(j, temp.next(j).next(j));
					}

					// if deleting makes maxHeight smaller, trim list
					this.size--;
					int maxHeight = getMaxHeight(size());

					if (maxHeight < height())
					{
						this.height = maxHeight;
						trimSkipList(head());
					}
					break;
				}
				else if (temp.next(i).value().compareTo(data) < 0)
					temp = temp.next(i);
				else break;
			}
		}
	}

	public boolean contains(AnyType data)
	{
		Node<AnyType> temp = head();

		// traverse same as usually, but...
		for (int i = height() - 1; i >= 0; i--)
		{
			while(temp.next(i) != null)
			{
				// if data values match the lists contains the node and you can return
				if (temp.next(i).value().compareTo(data) == 0)
					return true;
				if (temp.next(i).value().compareTo(data) > 0)
					break;

				temp = temp.next(i);
			}
		}

		// if you make it out traversal without returning anything,
		// value isn't in the list. return false
		return false;
	}

	// same as contains but replace true/false with temp.next(i)/null
	public Node<AnyType> get(AnyType data)
	{
		Node<AnyType> temp = head();

		for (int i = height() - 1; i >= 0; i--)
		{
			while(temp.next(i) != null)
			{
				if (temp.next(i).value().compareTo(data) == 0)
					return temp.next(i);
				if (temp.next(i).value().compareTo(data) > 0)
					break;

				temp = temp.next(i);
			}
		}

		return null;
	}

	private static int getMaxHeight(int n)
	{
		// make sure max height doesnt go lower than 1
		if (n == 1 || n == 0)
			return 1;

		return (int) Math.ceil(Math.log(n) / Math.log(2));
	}

	private static int generateRandomHeight(int maxHeight)
	{
		int height = 1;

		// need to flip to heads to add to height. stop adding to
		// height either when it reaches maxHeight or when you flip tails
		while(height != maxHeight)
		{
			if (Math.random() > 0.50)
				height++;
			else break;
		}

		return height;
	}

	private void growSkipList()
	{
		int level = height() - 1;

		// make sure to increases height of skiplist along with head
		this.head.grow();
		this.height++;

		Node<AnyType> temp = head();
		Node<AnyType> temp2 = head();

		while (temp.next(level) != null)
		{
			temp = temp.next(level);

			// if node grows, link previous node
			if(temp.maybeGrow())
			{
				temp2.setNext(level + 1, temp);
				temp2 = temp2.next(level + 1);
			}
		}
	}

	private void trimSkipList(Node<AnyType> node)
	{
		//use recursion to trim tallest nodes
		if (node == null) return;

		// use updated height of skip list to trim nodes
		// above this height, to this height
		trimSkipList(node.next(height() - 1));
		node.trim(height);
	}

	public static double difficultyRating()
	{
		return 5.0;
	}

	public static double hoursSpent()
	{
		return 36.0;
	}
}
