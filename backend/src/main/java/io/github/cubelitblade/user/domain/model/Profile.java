package io.github.cubelitblade.user.domain.model;

import java.util.Objects;

/**
 * Represents the profile details of an account.
 * <p>
 * This record is mapped to the {@code profile} JSONB column in the database.
 *
 * @param gender The gender code according to GB/T 2261.1-2003.
 *               Null values are normalized to {@code 0} (Unknown) by default.
 *               <ul>
 *                 <li>{@code 0} - Unknown</li>
 *                 <li>{@code 1} - Male</li>
 *                 <li>{@code 2} - Female</li>
 *                 <li>{@code 9} - Not specified</li>
 *               </ul>
 * @see <a href="https://openstd.samr.gov.cn/bzgk/std/newGbInfo?hcno=0FC942D542BC6EE3C707B2647EF81CD8">GB/T 2261.1-2003</a>
 */
public record Profile(Integer gender) {
    public Profile(Integer gender) {
        gender = Objects.requireNonNullElse(gender, 0);
        switch (gender) {
            case 0, 1, 2, 9 -> this.gender = gender;
            default -> throw new IllegalArgumentException("Invalid gender code: " + gender);
        }
    }
}
