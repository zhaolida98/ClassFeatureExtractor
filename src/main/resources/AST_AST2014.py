import os
import sys
import javalang
from javalang.ast import Node
from anytree import AnyNode, RenderTree

# AST的预处理，文章没有具体提及，后续添加


# 得到AST需要的数据，递归各节点遍历出一棵树 tree
def get_token(node):
    token = ''
    # print(isinstance(node, Node))
    # print(type(node))
    if isinstance(node, str):
        token = node
    elif isinstance(node, set):
        token = 'Modifier'
    elif isinstance(node, Node):
        token = node.__class__.__name__
    # print(node.__class__.__name__,str(node))
    # print(node.__class__.__name__, node)
    return token


def get_child(root):
    # print(root)
    if isinstance(root, Node):
        children = root.children
    elif isinstance(root, set):
        children = list(root)
    else:
        children = []

    def expand(nested_list):
        for item in nested_list:
            if isinstance(item, list):
                for sub_item in expand(item):
                    # print(sub_item)
                    yield sub_item
            elif item:
                # print(item)
                yield item

    return list(expand(children))


def createtree(root, node, nodelist, parent=None):
    id = len(nodelist)
    # print(id)
    token, children = get_token(node), get_child(node)
    if id == 0:
        root.token = token
        root.data = node
    else:
        newnode = AnyNode(id=id, token=token, data=node, parent=parent)
    nodelist.append(node)
    for child in children:
        if id == 0:
            createtree(root, child, nodelist, parent=root)
        else:
            createtree(root, child, nodelist, parent=newnode)


# 代码数据预处理
def Buildast(programfile):
    # programfile = open(r"test.java", encoding='utf-8')
    # print(os.path.join(rt,file))
    programtext = programfile.read()
    # programtext=programtext.replace('\r','')
    programtokens = javalang.tokenizer.tokenize(programtext)
    # print("programtokens",list(programtokens))
    parser = javalang.parse.Parser(programtokens)
    programast = parser.parse_member_declaration()
    # print(programast)
    tree = programast
    nodelist = []
    newtree = AnyNode(token=None, data=None)

    createtree(newtree, tree, nodelist)
    return newtree


def getNodenum(root):
    if root is None:
        return 0
    elif root.is_leaf:
        return 1
    else:
        res = 1
        for ch in root.children:
            res += getNodenum(ch)
        return res


def Hashforast(tree):
    tmptree = tree
    pass


def Treematch(tree1, tree2):
    """
    计算共同节点数
    """
    if tree1 is None or tree2 is None:
        return 0

    token1 = tree1.__dict__['token']
    token2 = tree2.__dict__['token']
    if token1 != token2:
        return 0

    ch_a = [x for x in tree1.children]
    ch_b = [x for x in tree2.children]
    m = len(ch_a)
    n = len(ch_b)
    # 动态规划计算最大匹配节点数
    res_m = [[0 for j in range(n + 1)] for i in range(m + 1)]
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            res_m[i][j] = max(
                res_m[i - 1][j], res_m[i][j - 1],
                res_m[i - 1][j - 1] + Treematch(ch_a[i - 1], ch_b[j - 1]))
    return res_m[m][n] + 1

def AST2014(f1,f2):
    file1 = open(f1)
    file2 = open(f2)
    tree1, tree2 = Buildast(file1), Buildast(file2)
    file1.close()
    file2.close()
    commonnodes = Treematch(tree1, tree2)
    similarity = 2 * commonnodes / (getNodenum(tree1) + getNodenum(tree2))
    #print(similarity)
    return similarity
    # print(RenderTree(tree1))


