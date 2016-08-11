package com.github.jyoghurt.core.dao;


import com.github.jyoghurt.core.domain.BaseEntity;
import com.github.jyoghurt.core.enums.LogSystemType;
import com.github.jyoghurt.core.exception.DaoException;
import com.github.jyoghurt.core.exception.UtilException;
import com.github.jyoghurt.core.handle.CustomWhereHandle;
import com.github.jyoghurt.core.handle.OperatorHandle;
import com.github.jyoghurt.core.handle.SQLJoinHandle;
import com.github.jyoghurt.core.utils.JPAUtils;
import com.github.jyoghurt.core.utils.beanUtils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static com.github.jyoghurt.core.mybatis.SqlBuilder.*;
import static com.github.jyoghurt.core.mybatis.SqlBuilder.WHERE;

/**
 * Created by jtwu on 2015/4/21.
 */
@SuppressWarnings("unchecked")
public class BaseMapperProvider {
    /**
     * 用于保存解析出来的级联关系
     * 对于配置了级联关系的对象（使用了@OneToOne等注解的），如果子对象不为空，框架自动拼装级联查询条件，类似Hibernate效果
     */
    private List<SQLJoinHandle> sqlJoinHandles;
    private Class<?> entityClass;
    protected String limit;

    private void begin() {
        reset();
        BEGIN();
    }

    private void reset() {
        sqlJoinHandles = new ArrayList<>();
    }

    private String sql() {
        reset();
        return SQL();
    }

    /**
     * 查询所有数据sql
     *
     * @param param 参数
     * @return sql
     * @throws DaoException {@inheritDoc}
     */
    public String findAll(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        if (((Map<String, Object>) param.get(BaseMapper.DATA)).containsKey("distinct")) {
            SELECT_DISTINCT("t.*");
        } else {
            SELECT("t.*");
        }
        FROM(getTableNameWithAlias(entityClass));
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false);
        return StringUtils.join(sql(), limit);
    }

    protected void createAllWhere(Map<String, Object> param, boolean usePage) throws DaoException {
        createAllWhere(param, usePage, false);
    }

    protected void createAllWhere(Map<String, Object> param, boolean usePage, boolean isCount) throws DaoException {
        if (MapUtils.isEmpty(param)) {
            return;
        }
        try {
            Map<String, OperatorHandle> operatorMap = param.containsKey("operatorHandles") ?
                    (Map) param.get("operatorHandles") : new HashMap();
            if (CollectionUtils.isNotEmpty((LinkedList) param.get("customList"))) {
                boolean operatorTag = false;
                for (CustomWhereHandle customWhereHandle : (LinkedList<CustomWhereHandle>) param.get("customList")) {
                    switch (customWhereHandle.getType()) {
                        case FIELD: {
                            operatorTag = createFieldWhereSql(operatorMap, entityClass.getDeclaredField
                                    (customWhereHandle.getSql()), param);
                            break;
                        }
                        case OR: {
                            if (operatorTag) {
                                OR();
                                operatorTag = false;
                            }
                            break;
                        }
                        case AND: {
                            if (operatorTag) {
                                AND();
                                operatorTag = false;
                            }
                            break;
                        }
                        case SQL: {
                            WHERE(customWhereHandle.getSql());
                            operatorTag = true;
                            break;
                        }
                    }
                }
                parseQueryHandle(param, usePage, isCount);
                return;
            }

            createFieldsWhereSql(operatorMap, param);
            parseQueryHandle(param, usePage, isCount);
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }

    private boolean createFieldWhereSql(Map<String, OperatorHandle> operatorMap, Field declaredField, Map<String, Object> param) throws UtilException {
        return createFieldWhereSql(operatorMap, declaredField, param, null);
    }

    private boolean createFieldWhereSql(Map<String, OperatorHandle> operatorMap, Field field, Map<String, Object>
            param, String tableAlias) throws UtilException {
        //排除非持久化字段和Class字段
        if (null != field.getAnnotation(Transient.class) || field.getType().isAssignableFrom(Class.class)) {
            return false;
        }
        //排除为空并且未出现的自定义查询条件的字段

        //modify by limiao 20160811  处理拼in的时候，数组为空，不想查询的问题。当数组为空的时候，仍然继续 start
//        if (StringUtils.isEmpty(tableAlias) && !param.containsKey(field.getName()) && (!operatorMap.containsKey(field.getName()) || ArrayUtils.isEmpty(
//                operatorMap.get(field.getName()).getValues()))) {
//            return false;
//        }
        if (StringUtils.isEmpty(tableAlias) && !param.containsKey(field.getName()) && (!operatorMap.containsKey(field.getName()))) {
            return false;
        }
        if ((operatorMap.containsKey(field.getName())) && operatorMap.get(field.getName()).getValues() == null) {
            return false;
        }
        //如果是级联获取的，上一逻辑需要加上表前缀
        String fieldNameKey = StringUtils.isEmpty(tableAlias) ? field.getName() : tableAlias + "." + field.getName();
//        if (StringUtils.isNotEmpty(tableAlias) && null == JPAUtils.getValue(param.get(tableAlias), field.getName()) &&
//                (!operatorMap.containsKey(fieldNameKey) || ArrayUtils.isEmpty(operatorMap.get(fieldNameKey).getValues()))) {
//            return false;
//        }
        if (StringUtils.isNotEmpty(tableAlias) && null == JPAUtils.getValue(param.get(tableAlias), field.getName()) &&
                (!operatorMap.containsKey(fieldNameKey)) || operatorMap.get(fieldNameKey).getValues() == null) {
            return false;
        }
        //modify by limiao 20160811  处理拼in的时候，数组为空，不想查询的问题 end

        //封装类型递归处理，拼装成级联查询
        if (null != field.getType().getAnnotation(Table.class)) {
            parseCascade(operatorMap, field, param);
            return false;
        }
        String prefix = StringUtils.isEmpty(tableAlias) ? "t." : StringUtils.join(tableAlias, ".");
        if (!operatorMap.containsKey(fieldNameKey)) {
            WHERE(StringUtils.join(prefix, getEqualsValue(field
                    .getName(), StringUtils.join(addSuffix(BaseMapper.DATA + ".", tableAlias), field.getName()))));
            return true;
        }
        /*
         * 将参数变量的"."改成"_"，eg table.id改成table_id,
         * 不改mybatis会将解释成table对象的id属性，而非table.id变量
         */

        for (String key : operatorMap.keySet()) {
            if (key.contains(".")) {
                operatorMap.put(key.replace(".", "_"), operatorMap.get(key));
            }
        }
        //用户自定义值优先
        Object value = ArrayUtils.isEmpty((operatorMap.get(fieldNameKey)).getValues())
                ? "#{" + StringUtils.join(BaseMapper.DATA + "." + fieldNameKey) + "}"
                : "#{" + BaseMapper.DATA + ".operatorHandles." + fieldNameKey.replace(".", "_") + ".values[0]}";
        switch ((operatorMap.get(fieldNameKey)).getOperator()) {
            case EQUAL: {
                WHERE(StringUtils.join(prefix, field.getName(), " = ", value));
                break;
            }
            case LIKE: {
                WHERE(StringUtils.join(prefix, field.getName(), " like CONCAT('%',", value, ", '%')"));
                break;
            }
            case LESS_THEN: {
                WHERE(StringUtils.join(prefix, field.getName(), " < ", value));
                break;
            }
            case MORE_THEN: {
                WHERE(StringUtils.join(prefix, field.getName(), " > ", value));
                break;
            }
            case LESS_EQUAL: {
                WHERE(StringUtils.join(prefix, field.getName(), " <= ", value));
                break;
            }
            case MORE_EQUAL: {
                WHERE(StringUtils.join(prefix, field.getName(), " >= ", value));
                break;
            }
            case IN: {
                String inValue = "";
                for (int i = 0; i < operatorMap.get(fieldNameKey).getValues().length; i++) {
                    inValue += " #{" + BaseMapper.DATA + ".operatorHandles." + field.getName() + ".values[" + i + "]},";
                }
                //add by limiao 20160811 处理拼in的时候，数组为空，不想查询的问题
                if (StringUtils.isEmpty(inValue)) {
                    WHERE(StringUtils.join(prefix, field.getName(), " in ( null )"));
                    //效率高的方案...
                    //WHERE("1=2");
                } else {
                    WHERE(StringUtils.join(prefix, field.getName(), " in (", inValue.substring(0, inValue.length() - 1), ")" +
                            ""));
                }
                break;
            }
            case FIND_IN_SET: {
                WHERE("find_in_set(" + value + ",t." + field.getName() + ")");
                break;
            }
            /* add by limiao 20160203 ,新增NOT_EQUAL, NOT_LIKE,IS_NULL ,IS_NOT_NULL   */
            case NOT_EQUAL: {
                WHERE(StringUtils.join(prefix, getNotEqualsValue(field.getName(), StringUtils.join(addSuffix(BaseMapper.DATA + ".", tableAlias), field.getName()))));
                break;
            }
            case NOT_LIKE: {
                WHERE(StringUtils.join(prefix, field.getName(), " not like CONCAT('%'," + value + ", '%')"));
                break;
            }
            case IS_NULL: {
                WHERE(StringUtils.join(prefix, field.getName(), " is null "));
                break;
            }
            case IS_NOT_NULL: {
                WHERE(StringUtils.join(prefix, field.getName(), " is not null "));
                break;
            }
        }
        return true;
    }

    private void parseCascade(Map<String, OperatorHandle> operatorMap, Field field, Map<String, Object> param) throws UtilException {
        //不是PO不做任何处理,没有配置级联关系不处理
        if (null == field.getType().getAnnotation(Table.class) || (null == field.getAnnotation(OneToMany.class)
                && null == field.getAnnotation(OneToOne.class) && null == field.getAnnotation(ManyToMany.class)
                && null == field.getAnnotation(ManyToOne.class))) {
            return;
        }
        String joinTableName = field.getType().getSimpleName();
        String foreignKey = null == field.getAnnotation(JoinColumn.class)
                ? JPAUtils.getIdField(field.getType()).getName() : (field.getAnnotation(JoinColumn.class))
                .referencedColumnName();
        //添加级联
        SQLJoinHandle sqlJoinHandle = new SQLJoinHandle().setSelectColumns(field.getName() + ".*")
                .setJoinType(SQLJoinHandle.JoinType.LEFT_OUTER_JOIN).setJoinSql(StringUtils.join(joinTableName, " ",
                        field.getName(), " on t.", JPAUtils.getIdField(field.getType()).getName(), "=", field.getName(), ".", foreignKey));
        sqlJoinHandles.add(sqlJoinHandle);
        createFieldsWhereSql(operatorMap, param, field.getName(), field.getType());
        return;

    }


    private String addSuffix(String prefix, String tableAlias) {
        return StringUtils.join(prefix, StringUtils.isEmpty(tableAlias) ? "" : tableAlias + ".");
    }

    private void createFieldsWhereSql(Map<String, OperatorHandle> operatorMap, Map<String, Object> param) throws UtilException {
        createFieldsWhereSql(operatorMap, param, null, entityClass);
    }

    private void createFieldsWhereSql(Map<String, OperatorHandle> operatorMap, Map<String, Object> param,
                                      String tableAlias, Class clazz) throws UtilException {
        for (Field field : JPAUtils.getAllFields(clazz)) {
            createFieldWhereSql(operatorMap, field, param, tableAlias);
        }
    }

    /*
     * 处理查询扩展类
     */
    private void parseQueryHandle(Map<String, Object> param, Boolean usePage, Boolean isCount) {
        if (param.containsKey("orderBy") && !isCount) {
            LinkedHashMap<String, String> orderByMap = (LinkedHashMap<String, String>) param.get("orderBy");
            for (String orderBy : orderByMap.keySet()) {
                if (orderBy.contains(".")) {
                    ORDER_BY(orderBy + " " + orderByMap.get(orderBy));
                    continue;
                }
                ORDER_BY(StringUtils.join("t.", orderBy, " ", orderByMap.get(orderBy)));
            }
        }
        if (param.containsKey("whereSqls")) {
            List<String> whereSqls = (List<String>) param.get("whereSqls");
            for (String whereSql : whereSqls) {
                WHERE(whereSql);
            }
        }
        createLimit(param, usePage);
        if (param.containsKey("sqlJoinHandle")) {
            sqlJoinHandles.addAll((List) param.get("sqlJoinHandle"));
        }
        for (SQLJoinHandle sqlJoinAssistor : sqlJoinHandles) {
            switch (sqlJoinAssistor.getJoinType()) {
                case JOIN: {
                    JOIN(sqlJoinAssistor.getJoinSql());
                    if (StringUtils.isNotEmpty(sqlJoinAssistor.getSelectColumns()) && !isCount) {
                        SELECT(sqlJoinAssistor.getSelectColumns());
                    }
                    break;
                }
                case INNER_JOIN: {
                    INNER_JOIN(sqlJoinAssistor.getJoinSql());
                    if (StringUtils.isNotEmpty(sqlJoinAssistor.getSelectColumns()) && !isCount) {
                        SELECT(sqlJoinAssistor.getSelectColumns());
                    }
                    break;
                }
                case LEFT_OUTER_JOIN: {
                    LEFT_OUTER_JOIN(sqlJoinAssistor.getJoinSql());
                    if (StringUtils.isNotEmpty(sqlJoinAssistor.getSelectColumns()) && !isCount) {
                        SELECT(sqlJoinAssistor.getSelectColumns());
                    }
                    break;
                }
                case RIGHT_OUTER_JOIN: {
                    RIGHT_OUTER_JOIN(sqlJoinAssistor.getJoinSql());
                    if (StringUtils.isNotEmpty(sqlJoinAssistor.getSelectColumns()) && !isCount) {
                        SELECT(sqlJoinAssistor.getSelectColumns());
                    }
                    break;
                }
            }
        }
        if (param.containsKey("groupBy")) {
            GROUP_BY((String) param.get("groupBy"));
        }
    }

    /**
     * 创建limit
     *
     * @param param   参数
     * @param usePage 是否使用分页
     */
    private void createLimit(Map<String, Object> param, Boolean usePage) {
        if (param.containsKey("page") && usePage) {
            limit = StringUtils.join(" limit ", ((Integer) param.get("page") - 1) * (Integer) param.get("rows"), " , ",
                    param.get("rows").toString());
        }
    }

    /**
     * 查询所有数据sql
     *
     * @param param 参数
     * @return sql
     * @throws DaoException {@inheritDoc}
     */
    public String selectById(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        SELECT("*");
        FROM(getTableName(entityClass));
        try {
            WHERE(getEqualsValue(JPAUtils.getIdField(entityClass).getName(), BaseMapper.ID));
        } catch (UtilException e) {
            throw new DaoException(e);
        }
        return sql();
    }

    /**
     * 获取比较值字符串
     *
     * @param column 列名
     * @param value  值
     * @return 拼接后的字符串
     */
    private String getEqualsValue(String column, String value) {
        return StringUtils.join(column, " = #{", value, "}");
    }

    /**
     * 获取不等于比较值字符串
     * <p>
     * add by limiao 20160203
     *
     * @param column 列名
     * @param value  值
     * @return 拼接后的字符串
     */
    private String getNotEqualsValue(String column, String value) {
        return StringUtils.join(column, " != #{", value, "}");
    }

    /*
     * 生成sql开始
     */
    protected void beginWithClass(Map<String, Object> param) throws DaoException {
        if (null == param.get(BaseMapper.ENTITY_CLASS)) {
            throw new DaoException(StringUtils.join("获取实体类型失败 entityClass 为空"));
        }
        begin();
        entityClass = (Class) param.get(BaseMapper.ENTITY_CLASS);
    }


    /**
     * 保存sql
     *
     * @param param 参数
     * @return sql
     * @throws DaoException {@inheritDoc}
     */
    public String save(Map<String, Object> param) throws Exception {
        entityClass = param.get(BaseMapper.ENTITY).getClass();
        begin();
        INSERT_INTO(getTableName(entityClass));

        Field idField = null;
        for (Field field : JPAUtils.getAllFields(entityClass)) {
            if (!ClassUtils.isPrimitiveOrWrapper(field.getClass()) && !Enum.class.isAssignableFrom(LogSystemType.class)) {
                continue;
            }
            field.setAccessible(true);
            //处理主键
            if (null != field.getAnnotation(Id.class) || null != field.getAnnotation(Transient.class)) {
                idField = field;
                continue;
            }
            //add by limiao 20160309 insert 为null的不拼sql
            try {
                Object value = JPAUtils.getValue(param.get(BaseMapper.ENTITY), field.getName());
                if (null == value) {
                    continue;
                }
            } catch (UtilException e) {
                throw new DaoException(e);
            }

            VALUES(field.getName(), StringUtils.join("#{", BaseMapper.ENTITY, ".", field.getName(), "}"));
        }
        if (null == idField) {
            throw new DaoException(StringUtils.join(entityClass.getName(), "实体未配置@Id "));
        }

        setId(param, idField);
        return sql();
    }

    /**
     * 保存sql
     *
     * @param param 参数
     * @return sql
     * @throws DaoException {@inheritDoc}
     */
    public String saveBatch(Map<String, Object> param) throws Exception {
        if (null == param.get(BaseMapper.ENTITIES)) {
            throw new DaoException("批量插入的数据为null");
        }
        entityClass = ((List) param.get(BaseMapper.ENTITIES)).get(0).getClass();
        begin();
        BATCH_INSERT_INTO(getTableName(entityClass));

        Field idField = null;
        for (int i = 0; i < ((List) param.get(BaseMapper.ENTITIES)).size(); i++) {
            for (Field field : JPAUtils.getAllFields(entityClass)) {

                if (!ClassUtils.isPrimitiveOrWrapper(field.getClass()) && !Enum.class.isAssignableFrom(LogSystemType.class)) {
                    continue;
                }
                field.setAccessible(true);
                //处理主键
                if (null != field.getAnnotation(Id.class) || null != field.getAnnotation(Transient.class)) {
                    idField = field;
                    continue;
                }
                //add by limiao 20160309 insert 为null的不拼sql
                try {
                    Object value = JPAUtils.getValue(((List) param.get(BaseMapper.ENTITIES)).get(i), field.getName());
                    if (null == value) {
                        continue;
                    }
                } catch (UtilException e) {
                    throw new DaoException(e);
                }
                BATCH_VALUES(field.getName(), StringUtils.join("#{", BaseMapper.ENTITIES, "[", i, "]", ".", field.getName(), "}"));
            }
            if (null == idField) {
                throw new DaoException(StringUtils.join(entityClass.getName(), "实体未配置@Id "));
            }
            setIdBatch((BaseEntity) ((List) param.get(BaseMapper.ENTITIES)).get(i), i, idField);
            BATCH_SEGMENTATION();
        }
        return sql();
    }


    public String saveForSelective(Map<String, Object> param) throws Exception {
        final Object entity = param.get(BaseMapper.ENTITY);
        entityClass = entity.getClass();
        begin();
        INSERT_INTO(getTableName(entityClass));

        Field idField = null;
        for (Field field : JPAUtils.getAllFields(entityClass)) {
            field.setAccessible(true);
            //处理主键
            if (null != field.getAnnotation(Id.class) || null != field.getAnnotation(Transient.class)) {
                idField = field;
                continue;
            }
            if (field.get(entity) != null) {
                VALUES(field.getName(), StringUtils.join("#{", BaseMapper.ENTITY, ".", field.getName(), "}"));
            }
        }
        if (null == idField) {
            throw new DaoException(StringUtils.join(entityClass.getName(), "实体未配置@Id "));
        }
        setId(param, idField);
        return sql();
    }


    private void setId(Map<String, Object> param, Field idField) throws IllegalAccessException {
        if (!idField.getType().isAssignableFrom(String.class)) {
            return;
        }
        if (StringUtils.isNotEmpty((String) idField.get(param.get(BaseMapper.ENTITY)))) {
            VALUES(idField.getName(), StringUtils.join("#{", BaseMapper.ENTITY, ".", idField.getName(), "}"));
            return;
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        VALUES(idField.getName(), StringUtils.join("'", id, "'"));
        idField.set(param.get(BaseMapper.ENTITY), id);
    }

    private void setIdBatch(BaseEntity baseEntity, int i, Field idField) throws IllegalAccessException {
        if (!idField.getType().isAssignableFrom(String.class)) {
            return;
        }
        if (StringUtils.isNotEmpty((String) idField.get(baseEntity))) {
            BATCH_VALUES(idField.getName(), StringUtils.join("#{", BaseMapper.ENTITIES, "[", i, "]", ".", idField.getName(), "}"));
            return;
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        BATCH_VALUES(idField.getName(), StringUtils.join("'", id, "'"));
        idField.set(baseEntity, id);
    }

    public String update(Map<String, Object> param) throws DaoException {
        entityClass = param.get(BaseMapper.ENTITY).getClass();
        begin();
        UPDATE(getTableName(entityClass));
        Field idField = null;
        for (Field field : JPAUtils.getAllFields(entityClass)) {
            field.setAccessible(true);
            //非基础类型不处理
            if (field.getType().isAssignableFrom(Collection.class) || null != field.getType().getAnnotation(Table.class)) {
                continue;
            }
            //处理主键
            if (null == field.getAnnotation(Id.class)) {
                SET(StringUtils.join(getEqualsValue(field.getName(), StringUtils.join(BaseMapper.ENTITY, ".", field.getName()))));
                continue;
            }
            idField = field;
            WHERE(getEqualsValue(field.getName(), StringUtils.join(BaseMapper.ENTITY, ".", field.getName())));
        }
        if (null == idField) {
            throw new DaoException(StringUtils.join(entityClass.getName(), "实体未配置@Id "));
        }


        return sql();

    }

    /**
     * 获取表名
     *
     * @param entityClass 实体类
     * @return sql
     * @throws DaoException {@inheritDoc}
     */
    protected String getTableName(Class entityClass) throws DaoException {
        Annotation table = entityClass.getAnnotation(Table.class);
        if (null == table) {
            throw new DaoException(StringUtils.join("实体未配置Table注解 entityClass =", entityClass.getName()));
        }
        String tableName = ((Table) table).name();
        if (StringUtils.isEmpty(tableName)) {
            throw new DaoException(StringUtils.join("实体的Table注解未配置name属性 entityClass =", entityClass.getName()));
        }
        return tableName;
    }

    protected String getTableNameWithAlias(Class entityClass) throws DaoException {
        return StringUtils.join(getTableName(entityClass), " t");
    }

    public String pageData(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        if (((Map<String, Object>) param.get(BaseMapper.DATA)).containsKey("distinct")) {
            SELECT_DISTINCT("t.*");
        } else {
            SELECT("t.*");
        }
        FROM(getTableNameWithAlias(entityClass));
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), true);
        return StringUtils.join(sql(), limit);
    }

    public String pageTotalRecord(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        try {
            SELECT("count(distinct t." + JPAUtils.getIdField((Class<?>) param.get(BaseMapper.ENTITY_CLASS)).getName() + ")");
        } catch (UtilException e) {
            throw new DaoException("Query paging failure !");
        }
        FROM(getTableNameWithAlias(entityClass));
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false, true);
        return sql();
    }

    public String findListBySql(Map<String, Object> param) throws DaoException {
        createLimit((Map<String, Object>) param.get(BaseMapper.DATA), true);
        return StringUtils.join(param.get(BaseMapper.CUSTOM_SQL), limit);
    }

    public String findListTotalRecordBySql(Map<String, Object> param) throws DaoException {
        return StringUtils.join("select count(*) from (", param.get(BaseMapper.CUSTOM_SQL), ") as countTable");
    }

    public String findUniqueObjectBySql(Map<String, Object> param) throws DaoException {
        return StringUtils.join(param.get(BaseMapper.CUSTOM_SQL), " limit 1");
    }

    public String delete(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        DELETE_FROM(getTableName(entityClass));
        try {
            WHERE(getEqualsValue(JPAUtils.getIdField(entityClass).getName(), BaseMapper.ID));
        } catch (UtilException e) {
            throw new DaoException(e);
        }
        return sql();
    }

    public String logicDelete(Map<String, Object> param) throws DaoException {
        entityClass = (Class<?>) param.get(BaseMapper.ENTITY_CLASS);
        begin();
        UPDATE(getTableName(entityClass));
        param.put(BaseEntity.DELETE_FLAG, true);
        SET(getEqualsValue(BaseEntity.DELETE_FLAG, BaseEntity.DELETE_FLAG));
        try {
            Field idField = JPAUtils.getIdField(entityClass);
            WHERE(getEqualsValue(idField.getName(), BaseMapper.ID));
        } catch (UtilException e) {
            throw new DaoException(e);
        }
        return sql();
    }

    public String logicDeleteByCondition(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false);
        UPDATE(StringUtils.join(getTableName(entityClass), " t"));
        param.put(BaseEntity.DELETE_FLAG, true);
        SET(getEqualsValue(BaseEntity.DELETE_FLAG, BaseEntity.DELETE_FLAG));
        return sql();
    }

    public String updateForSelective(Map<String, Object> param) throws DaoException {
        entityClass = param.get(BaseMapper.ENTITY).getClass();
        begin();
        UPDATE(getTableName(entityClass));
        Field idField = null;
        for (Field field : JPAUtils.getAllFields(entityClass)) {
            field.setAccessible(true);
            //非基础类型不处理
            if (field.getType().isAssignableFrom(Collection.class) || null != field.getType().getAnnotation(Table.class)) {
                continue;
            }
            //处理主键
            if (null != field.getAnnotation(Id.class)) {
                idField = field;
                continue;
            }
            try {
                Object value = JPAUtils.getValue(param.get(BaseMapper.ENTITY), field.getName());
                if (null == value) {
                    continue;
                }
                //空字符串对应修改时，情况输入框
//                if(StringUtils.EMPTY.equals(value)){
//                    continue;
//                }
            } catch (UtilException e) {
                throw new DaoException(e);
            }
            SET(StringUtils.join(getEqualsValue(field.getName(), StringUtils.join(BaseMapper.ENTITY, ".", field.getName()))));
        }
        if (null == idField) {
            throw new DaoException(StringUtils.join(entityClass.getName(), "实体未配置@Id "));
        }
        WHERE(getEqualsValue(idField.getName(), StringUtils.join(BaseMapper.ENTITY, ".", idField.getName())));
        return sql();
    }

    public String deleteByCondition(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false);
        DELETE_FROM(StringUtils.join(getTableName(entityClass), " t"));
        return sql().replaceFirst("DELETE FROM", "DELETE t FROM");
    }

    public String updateBySql(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        UPDATE(getTableNameWithAlias(entityClass));
        SET(param.get(BaseMapper.CUSTOM_SQL).toString());
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false);
        return SQL();
    }

    public String updateByCondition(Map<String, Object> param) throws DaoException {
        beginWithClass(param);
        UPDATE(getTableNameWithAlias(entityClass));
        SET("name = #{name}");
        createAllWhere((Map<String, Object>) param.get(BaseMapper.DATA), false);
        return StringUtils.join(sql(), limit);
    }


}
