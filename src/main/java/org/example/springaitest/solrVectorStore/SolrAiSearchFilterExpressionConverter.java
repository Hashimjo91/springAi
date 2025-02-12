package org.example.springaitest.solrVectorStore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.Filter.ExpressionType;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;

public class SolrAiSearchFilterExpressionConverter extends AbstractFilterExpressionConverter {
    private static final Pattern DATE_FORMAT_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public SolrAiSearchFilterExpressionConverter() {
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected void doExpression(Filter.Expression expression, StringBuilder context) {
        if (expression.type() != ExpressionType.IN && expression.type() != ExpressionType.NIN) {
            this.convertOperand(expression.left(), context);
            context.append(this.getOperationSymbol(expression));
            this.convertOperand(expression.right(), context);
        } else {
            context.append(this.getOperationSymbol(expression));
            context.append("(");
            this.convertOperand(expression.left(), context);
            this.convertOperand(expression.right(), context);
            context.append(")");
        }

    }

    protected void doStartValueRange(Filter.Value listValue, StringBuilder context) {
    }

    protected void doEndValueRange(Filter.Value listValue, StringBuilder context) {
    }

    protected void doAddValueRangeSpitter(Filter.Value listValue, StringBuilder context) {
        context.append(" OR ");
    }

    private String getOperationSymbol(Filter.Expression exp) {
        String var10000;
        switch (exp.type()) {
            case AND:
                var10000 = " AND ";
                break;
            case OR:
                var10000 = " OR ";
                break;
            case EQ:
            case IN:
                var10000 = "";
                break;
            case NE:
                var10000 = " NOT ";
                break;
            case LT:
                var10000 = "<";
                break;
            case LTE:
                var10000 = "<=";
                break;
            case GT:
                var10000 = ">";
                break;
            case GTE:
                var10000 = ">=";
                break;
            case NIN:
                var10000 = "NOT ";
                break;
            default:
                throw new RuntimeException("Not supported expression type: " + String.valueOf(exp.type()));
        }

        return var10000;
    }

    public void doKey(Filter.Key key, StringBuilder context) {
        String identifier = this.hasOuterQuotes(key.key()) ? this.removeOuterQuotes(key.key()) : key.key();
        context.append(identifier.trim()).append(":");
    }

    protected void doValue(Filter.Value filterValue, StringBuilder context) {
        Object var4 = filterValue.value();
        if (var4 instanceof List list) {
            int c = 0;
            Iterator var5 = list.iterator();

            while(var5.hasNext()) {
                Object v = var5.next();
                context.append(v);
                if (c++ < list.size() - 1) {
                    this.doAddValueRangeSpitter(filterValue, context);
                }
            }
        } else {
            this.doSingleValue(filterValue.value(), context);
        }

    }

    protected void doSingleValue(Object value, StringBuilder context) {
        if (value instanceof Date date) {
            context.append(this.dateFormat.format(date));
        } else if (value instanceof String text) {
            if (DATE_FORMAT_PATTERN.matcher(text).matches()) {
                try {
                    Date date = this.dateFormat.parse(text);
                    context.append(this.dateFormat.format(date));
                } catch (ParseException var6) {
                    throw new IllegalArgumentException("Invalid date type:" + text, var6);
                }
            } else {
                context.append(text);
            }
        } else {
            context.append(value);
        }

    }

    public void doStartGroup(Filter.Group group, StringBuilder context) {
        context.append("(");
    }

    public void doEndGroup(Filter.Group group, StringBuilder context) {
        context.append(")");
    }
}
