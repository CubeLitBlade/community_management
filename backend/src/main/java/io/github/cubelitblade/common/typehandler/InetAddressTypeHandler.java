package io.github.cubelitblade.common.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(InetAddress.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class InetAddressTypeHandler extends BaseTypeHandler<InetAddress> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InetAddress parameter, JdbcType jdbcType) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("inet");
        pgObject.setValue(parameter.getHostAddress());
        ps.setObject(i, pgObject);
    }

    @Override
    public InetAddress getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return toInetAddress(rs.getString(columnName));
    }

    @Override
    public InetAddress getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return toInetAddress(rs.getString(columnIndex));
    }

    @Override
    public InetAddress getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return toInetAddress(cs.getString(columnIndex));
    }

    private InetAddress toInetAddress(String ip) throws SQLException {
        if (ip == null) return null;
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new SQLException("Failed to convert string to InetAddress: " + ip, e);
        }
    }
}
