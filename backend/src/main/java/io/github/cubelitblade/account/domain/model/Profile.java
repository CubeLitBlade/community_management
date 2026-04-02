package io.github.cubelitblade.account.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public record Profile(Integer gender) {
    public Profile(Integer gender) {
        this.gender = switch (gender) {
            case null -> 0;
            case 0, 1, 2, 9 -> gender;
            default -> throw new IllegalArgumentException("Invalid gender code: " + gender);
        };
    }
}
