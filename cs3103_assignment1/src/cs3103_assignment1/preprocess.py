from __future__ import print_function
import sys, collections, re


filename = sys.argv[1]
output = open('output.txt', 'w')
regex_compile = re.compile(r'{(.*)}')
global_set = set()
count = 0

class OrderedSet(collections.Set):
    """
      Used to preserve the elements while set
      operation.
    """

    def __init__(self, iterable=()):
        self.d = collections.OrderedDict.fromkeys(iterable)

    def __len__(self):
        return len(self.d)

    def __contains__(self, element):
        return element in self.d

    def __iter__(self):
        return iter(self.d)

def _generator(lists):
    """flattens the list"""
    for x in lists:
        if isinstance(x, (list, set)):
            for i in x:
                yield i
        else:
            yield x

def _intersection(seq):
    """ Removes Duplicates without changing the order"""
    prev = None
    res_seq = []
    for e in seq:
        if e == prev:
            continue
        prev = e
        res_seq.append(e)
    return res_seq


def _process_path(as_path, duplicate=False):
    """
     Takes in a path and splits
     it into key and value
     for example:
       201 34 56 789  is transformed to
      4, [201, 34, 56, 789]
    """
    if duplicate:
        as_path = reduce(lambda x, y: x + y.lstrip(), re.split("{.*}", as_path))
    as_path = as_path.lstrip()
    return as_path
    



def get_line_data(line):
    """get a line and give appropriate data"""
    global count
    parser = lambda x: x.split(":")[1] if x != "" else x
    res = regex_compile.search(line)
    if not res:
		values= _process_path(parser(line), duplicate=False)
		values = values.split()
		values = OrderedSet(values)
		str_value = ' '.join(values)
		if str_value not in global_set:
			global_set.add(str_value)
			count = count + len(values)
			print(str_value, file=output)
		
with open(filename, 'r') as f:
	line = f.readline()
	while line:
		get_line_data(line)
		line = f.readline()
	print("The number of as path is {}".format(str(len(global_set))))
	print("The number of as's is {}".format(str(count)))


