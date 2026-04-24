(function () {
    const textarea = document.getElementById('markdown-input');
    const preview = document.getElementById('markdown-preview');
    const fileInput = document.getElementById('file-input');
    const fileList = document.getElementById('file-list');
    const toolbar = document.querySelector('[data-editor-toolbar]');

    if (!textarea || !preview) {
        return;
    }

    let requestId = 0;

    const renderPreview = async () => {
        const currentId = ++requestId;
        const formData = new FormData();
        formData.append('content', textarea.value || '');

        try {
            const response = await fetch('/admin/markdown/preview', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                },
            });

            if (!response.ok) {
                throw new Error('预览渲染失败');
            }

            const html = await response.text();
            if (currentId === requestId) {
                preview.innerHTML = html;
            }
        } catch (error) {
            if (currentId === requestId) {
                preview.innerHTML = '<p class="empty-markdown">预览加载失败，请稍后重试。</p>';
            }
        }
    };

    const debounce = (fn, delay) => {
        let timer;
        return (...args) => {
            window.clearTimeout(timer);
            timer = window.setTimeout(() => fn(...args), delay);
        };
    };

    const debouncedRender = debounce(renderPreview, 180);

    textarea.addEventListener('input', debouncedRender);
    renderPreview();

    if (toolbar) {
        toolbar.addEventListener('click', (event) => {
            const button = event.target.closest('button');
            if (!button) {
                return;
            }

            const prefix = button.dataset.prefix || '';
            const suffix = button.dataset.suffix || '';
            const placeholder = button.dataset.placeholder || '';
            const start = textarea.selectionStart;
            const end = textarea.selectionEnd;
            const selected = textarea.value.slice(start, end) || placeholder;
            const replacement = prefix + selected + suffix;
            textarea.setRangeText(replacement, start, end, 'end');
            textarea.focus();
            debouncedRender();
        });
    }

    if (fileInput && fileList) {
        fileInput.addEventListener('change', () => {
            const files = Array.from(fileInput.files || []);
            fileList.innerHTML = '';
            files.forEach((file) => {
                const chip = document.createElement('span');
                chip.className = 'file-chip';
                chip.textContent = file.name;
                fileList.appendChild(chip);
            });
        });
    }
})();
