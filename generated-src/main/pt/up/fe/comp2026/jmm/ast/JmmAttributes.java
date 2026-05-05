package pt.up.fe.comp2026.jmm.ast;
import pt.up.fe.comp.jmm.ast.NodeAttribute;
public class JmmAttributes {
  public enum PROGRAM implements NodeAttribute{
    /** JmmNode(JmmKind.CLASS_DECL) classNode **/
    CLASS_NODE("classNode");
    final String key; PROGRAM(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum PACKAGE_DECL implements NodeAttribute{
    /** List<String> path **/
    PATH("path");
    final String key; PACKAGE_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum IMPORT_DECL implements NodeAttribute{
    /** List<String> path **/
    PATH("path");
    final String key; IMPORT_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum CLASS_DECL implements NodeAttribute{
    /** String name **/
    NAME("name"),
    /** String superName **/
    SUPER_NAME("superName");
    final String key; CLASS_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum STMT_ENTRY implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum EXPRESSION implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum CLASS_MEMBER implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum FIELD_DECL implements NodeAttribute{
    /** JmmNode(JmmKind.TYPE) typeNode **/
    TYPE_NODE("typeNode"),
    /** String name **/
    NAME("name");
    final String key; FIELD_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum METHOD_DECL implements NodeAttribute{
    /** boolean isStatic **/
    IS_STATIC("isStatic"),
    /** String visibility **/
    VISIBILITY("visibility"),
    /** JmmNode(JmmKind.TYPE) returnType **/
    RETURN_TYPE("returnType"),
    /** String name **/
    NAME("name");
    final String key; METHOD_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum TYPE implements NodeAttribute{
    /** String name **/
    NAME("name"),
    /** String s6 **/
    S6("s6"),
    /** List<String> dims **/
    DIMS("dims"),
    /** String s7 **/
    S7("s7");
    final String key; TYPE(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum VAR_DECL implements NodeAttribute{
    /** JmmNode(JmmKind.TYPE) typeNode **/
    TYPE_NODE("typeNode"),
    /** String name **/
    NAME("name");
    final String key; VAR_DECL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum PARAM implements NodeAttribute{
    /** JmmNode(JmmKind.TYPE) typeNode **/
    TYPE_NODE("typeNode"),
    /** String name **/
    NAME("name");
    final String key; PARAM(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum ASSIGNMENT implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; ASSIGNMENT(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum BLOCK implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum FOR_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum FOR_INIT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum FOR_COND implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum FOR_UPDATE implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum WHILE_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum DO_WHILE_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum IF_ELSE_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum IF_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum ASSIGN_STMT implements NodeAttribute{
    /** String var **/
    VAR("var");
    final String key; ASSIGN_STMT(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum ARRAY_STORE_STMT implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; ARRAY_STORE_STMT(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum RETURN_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum EXPR_STMT implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum PAREN_EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum IMPLICIT_THIS_CALL_EXPR implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; IMPLICIT_THIS_CALL_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum THIS_EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum NEW_EXPR implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; NEW_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum ARRAY_INITIALIZER implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum NEW_ARRAY_EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum PLUS_PLUS_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; PLUS_PLUS_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum MINUS_MINUS_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; MINUS_MINUS_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum PLUS_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; PLUS_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum MINUS_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; MINUS_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum UNARY_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; UNARY_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum INTEGER_LITERAL implements NodeAttribute{
    /** String value **/
    VALUE("value");
    final String key; INTEGER_LITERAL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum BOOL_LITERAL implements NodeAttribute{
    /** String value **/
    VALUE("value");
    final String key; BOOL_LITERAL(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum VAR_REF_EXPR implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; VAR_REF_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum BINARY_EXPR implements NodeAttribute{
    /** String op **/
    OP("op");
    final String key; BINARY_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum LENGTH_EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}

  public enum METHOD_CALL_EXPR implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; METHOD_CALL_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum FIELD_ACCESS_EXPR implements NodeAttribute{
    /** String name **/
    NAME("name");
    final String key; FIELD_ACCESS_EXPR(String key){this.key = key;}
    @Override
    public String getKey() { return key;}
  }

  public enum ARRAY_LOAD_EXPR implements NodeAttribute{
    /*NO ATTRIBUTES*/;
    public String getKey() { return null;}
}
}