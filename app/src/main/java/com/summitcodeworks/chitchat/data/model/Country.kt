package com.summitcodeworks.chitchat.data.model

/**
 * Represents a country with its code, name, and dialing code
 */
data class Country(
    val code: String,        // Country code (e.g., "US", "IN", "GB")
    val name: String,        // Country name (e.g., "United States", "India", "United Kingdom")
    val dialCode: String,    // Dialing code (e.g., "+1", "+91", "+44")
    val flag: String         // Flag emoji (e.g., "🇺🇸", "🇮🇳", "🇬🇧")
)

/**
 * Default countries list with common countries
 */
object Countries {
    val list = listOf(
        Country("US", "United States", "+1", "🇺🇸"),
        Country("IN", "India", "+91", "🇮🇳"),
        Country("GB", "United Kingdom", "+44", "🇬🇧"),
        Country("CA", "Canada", "+1", "🇨🇦"),
        Country("AU", "Australia", "+61", "🇦🇺"),
        Country("DE", "Germany", "+49", "🇩🇪"),
        Country("FR", "France", "+33", "🇫🇷"),
        Country("IT", "Italy", "+39", "🇮🇹"),
        Country("ES", "Spain", "+34", "🇪🇸"),
        Country("BR", "Brazil", "+55", "🇧🇷"),
        Country("MX", "Mexico", "+52", "🇲🇽"),
        Country("JP", "Japan", "+81", "🇯🇵"),
        Country("KR", "South Korea", "+82", "🇰🇷"),
        Country("CN", "China", "+86", "🇨🇳"),
        Country("RU", "Russia", "+7", "🇷🇺"),
        Country("SA", "Saudi Arabia", "+966", "🇸🇦"),
        Country("AE", "United Arab Emirates", "+971", "🇦🇪"),
        Country("EG", "Egypt", "+20", "🇪🇬"),
        Country("ZA", "South Africa", "+27", "🇿🇦"),
        Country("NG", "Nigeria", "+234", "🇳🇬"),
        Country("KE", "Kenya", "+254", "🇰🇪"),
        Country("GH", "Ghana", "+233", "🇬🇭"),
        Country("MA", "Morocco", "+212", "🇲🇦"),
        Country("TN", "Tunisia", "+216", "🇹🇳"),
        Country("DZ", "Algeria", "+213", "🇩🇿"),
        Country("ET", "Ethiopia", "+251", "🇪🇹"),
        Country("UG", "Uganda", "+256", "🇺🇬"),
        Country("TZ", "Tanzania", "+255", "🇹🇿"),
        Country("ZW", "Zimbabwe", "+263", "🇿🇼"),
        Country("BW", "Botswana", "+267", "🇧🇼"),
        Country("NA", "Namibia", "+264", "🇳🇦"),
        Country("ZM", "Zambia", "+260", "🇿🇲"),
        Country("MW", "Malawi", "+265", "🇲🇼"),
        Country("MZ", "Mozambique", "+258", "🇲🇿"),
        Country("MG", "Madagascar", "+261", "🇲🇬"),
        Country("MU", "Mauritius", "+230", "🇲🇺"),
        Country("SC", "Seychelles", "+248", "🇸🇨"),
        Country("RE", "Réunion", "+262", "🇷🇪"),
        Country("YT", "Mayotte", "+262", "🇾🇹"),
        Country("KM", "Comoros", "+269", "🇰🇲"),
        Country("DJ", "Djibouti", "+253", "🇩🇯"),
        Country("SO", "Somalia", "+252", "🇸🇴"),
        Country("ER", "Eritrea", "+291", "🇪🇷"),
        Country("SD", "Sudan", "+249", "🇸🇩"),
        Country("SS", "South Sudan", "+211", "🇸🇸"),
        Country("CF", "Central African Republic", "+236", "🇨🇫"),
        Country("TD", "Chad", "+235", "🇹🇩"),
        Country("NE", "Niger", "+227", "🇳🇪"),
        Country("ML", "Mali", "+223", "🇲🇱"),
        Country("BF", "Burkina Faso", "+226", "🇧🇫"),
        Country("CI", "Ivory Coast", "+225", "🇨🇮"),
        Country("LR", "Liberia", "+231", "🇱🇷"),
        Country("SL", "Sierra Leone", "+232", "🇸🇱"),
        Country("GN", "Guinea", "+224", "🇬🇳"),
        Country("GW", "Guinea-Bissau", "+245", "🇬🇼"),
        Country("GM", "Gambia", "+220", "🇬🇲"),
        Country("SN", "Senegal", "+221", "🇸🇳"),
        Country("MR", "Mauritania", "+222", "🇲🇷"),
        Country("CV", "Cape Verde", "+238", "🇨🇻"),
        Country("ST", "São Tomé and Príncipe", "+239", "🇸🇹"),
        Country("GQ", "Equatorial Guinea", "+240", "🇬🇶"),
        Country("GA", "Gabon", "+241", "🇬🇦"),
        Country("CG", "Republic of the Congo", "+242", "🇨🇬"),
        Country("CD", "Democratic Republic of the Congo", "+243", "🇨🇩"),
        Country("AO", "Angola", "+244", "🇦🇴"),
        Country("BI", "Burundi", "+257", "🇧🇮"),
        Country("RW", "Rwanda", "+250", "🇷🇼")
    )
    
    /**
     * Get country by code
     */
    fun getByCode(code: String): Country? {
        return list.find { it.code == code }
    }
    
    /**
     * Get default country based on locale (without GPS)
     * For now, returns US as default
     */
    fun getDefaultCountry(): Country {
        return getByCode("IN") ?: list.first()
    }
}
