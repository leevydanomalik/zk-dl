package cz.datalite.hibernate.type;


import java.sql.Types;

/**
 * Typ pro konverze textové DB reprezentace na enum
 */
@SuppressWarnings("WeakerAccess")
public class EnumCharType extends AbstractEnumType
{
    @Override
    protected int getSqlType()
    {
        return Types.VARCHAR ;
    }
}
