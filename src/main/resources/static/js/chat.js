const form = document.querySelector("#chat-form");
const input = document.querySelector("#message");
const messages = document.querySelector("#chat-messages");
const statusMessage = document.querySelector("#chat-status");
const sendButton = document.querySelector("#send-button");
const characterCount = document.querySelector("#character-count");

function addMessage(text, kind, metadata) {
    const article = document.createElement("article");
    article.className = `chat-message ${kind}-message`;

    const label = document.createElement("span");
    label.className = "message-label";
    label.textContent = kind === "user" ? "VOCÊ" : "ASSISTENTE";

    const content = document.createElement("p");
    content.textContent = text;
    article.append(label, content);

    if (metadata) {
        const detail = document.createElement("small");
        detail.className = "message-detail";
        detail.textContent = `${metadata.provedor} · ${metadata.modelo} · ${new Date(metadata.dataHora).toLocaleString("pt-BR")}`;
        article.append(detail);
    }

    messages.append(article);
    messages.scrollTop = messages.scrollHeight;
}

async function loadHistory() {
    try {
        const response = await fetch("/api/historico");
        if (!response.ok) {
            throw new Error("Não foi possível carregar o histórico.");
        }
        const history = await response.json();
        messages.replaceChildren();
        if (history.length === 0) {
            addMessage("Olá! Posso ajudar você a conhecer os produtos do nosso mercado. O que está procurando?", "assistant");
            return;
        }
        history.reverse().forEach((item) => {
            addMessage(item.pergunta ?? "Pergunta anterior", "user");
            addMessage(item.resposta, "assistant", item);
        });
    } catch (error) {
        statusMessage.textContent = error.message;
    }
}

input.addEventListener("input", () => {
    characterCount.textContent = `${input.value.length}/1000`;
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const mensagem = input.value.trim();
    if (!mensagem) {
        statusMessage.textContent = "Escreva uma pergunta antes de enviar.";
        input.focus();
        return;
    }

    statusMessage.textContent = "Consultando o assistente...";
    sendButton.disabled = true;
    addMessage(mensagem, "user");
    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ mensagem })
        });
        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.mensagem ?? "Não foi possível enviar a pergunta.");
        }
        addMessage(result.resposta, "assistant", result);
        input.value = "";
        characterCount.textContent = "0/1000";
        statusMessage.textContent = "";
    } catch (error) {
        statusMessage.textContent = error.message;
    } finally {
        sendButton.disabled = false;
        input.focus();
    }
});

document.querySelector("#history-refresh").addEventListener("click", loadHistory);
loadHistory();
