package dev.chungjungsoo.gptmobile.presentation.common

object Route {

    const val GET_STARTED = "get_started"

    const val SETUP_ROUTE = "setup_route"
    const val SELECT_PLATFORM = "select_platform"
    const val TOKEN_INPUT = "token_input"
    const val OPENAI_MODEL_SELECT = "openai_model_select"
    const val ANTHROPIC_MODEL_SELECT = "anthropic_model_select"
    const val GOOGLE_MODEL_SELECT = "google_model_select"
    const val GROQ_MODEL_SELECT = "groq_model_select"
    const val OLLAMA_MODEL_SELECT = "ollama_model_select"
    const val OLLAMA_API_ADDRESS = "ollama_api_address"
    const val SETUP_COMPLETE = "setup_complete"

    const val CHAT_LIST = "chat_list"
    const val CHAT_ROOM = "chat_room/{chatRoomId}?enabled={enabledPlatforms}"

    const val VARIANTS = "variants"
    const val AUTOMATIONS = "automations"
    const val AGENTS = "agents"
    const val LIBRARY = "library"
    
    // Offline AI Model Routes
    const val OFFLINE_MODEL_BROWSER = "offlineModelBrowser"
    const val OFFLINE_MODEL_DETAIL = "offlineModelDetail/{modelId}"

    const val SETTING_ROUTE = "setting_route"
    const val SETTINGS = "settings"
    const val OPENAI_SETTINGS = "openai_settings"
    const val ANTHROPIC_SETTINGS = "anthropic_settings"
    const val GOOGLE_SETTINGS = "google_settings"
    const val GROQ_SETTINGS = "groq_settings"
    const val OLLAMA_SETTINGS = "ollama_settings"
    const val ABOUT_PAGE = "about"
    const val LICENSE = "license"
}
