import './style.css'
import { ActionTemplateConfig, ActionTemplatePlaceholder, httpAPI } from "./http";
import { ItemTypes, Semantics } from './resources';
import { AxiosError } from 'axios';

const ITEM_LABEL_PLACEHOLDER = "itemLabel";
const DYNAMIC_PLACEHOLDER = "*";
const STATE_PLACEHOLDER = "state";
const ITEM_OPTION_PLACEHOLDER = "itemOption";
const GROUP_LABEL_PLACEHOLDER = "groupLabel";
const RESERVED_WRITE_PLACEHOLDERS = [ITEM_LABEL_PLACEHOLDER, STATE_PLACEHOLDER, ITEM_OPTION_PLACEHOLDER, DYNAMIC_PLACEHOLDER, GROUP_LABEL_PLACEHOLDER];

const state = {
  actionList: [] as ActionTemplateConfig[],
  actionOnEdit: null as ActionTemplateConfig | null,
  actionOnEditOriginal: null as ActionTemplateConfig | null,
  placeholderOnEdit: null as ActionTemplatePlaceholder | null,
  placeholderOnEditOriginal: null as ActionTemplatePlaceholder | null,
  placeholderOnEditIsShared: false,
  sharedPlaceholders: [] as ActionTemplatePlaceholder[],
  actionElementMap: new Map<ActionTemplateConfig, HTMLElement>()
};

(async function () {
  let actions = [] as ActionTemplateConfig[];
  try {
    actions = await httpAPI.getActions();
  } catch (error) {
    if (error instanceof AxiosError && error.response?.status === 401) {
      await httpAPI.authorize();
      actions = await httpAPI.getActions();
    } else {
      throw error;
    }
  }
  state.sharedPlaceholders = await httpAPI.getPlaceholders();
  setupInputs();
  registerStaticEvents();
  const actionsContainer = queryAssertElement<HTMLDivElement>("#actions-container");
  for (const action of actions) {
    const el = renderActionArticle(action);
    state.actionElementMap.set(action, el);
    actionsContainer.appendChild(el);
  }
})().catch(err => console.error("Load error: ", err));

function setupInputs() {
  const selectItemType = queryAssertElement<HTMLDivElement>("#available-types");
  const selectItemSemantic = queryAssertElement<HTMLDivElement>("#available-semantics");
  const selectChildType = queryAssertElement<HTMLDivElement>("#child-types");
  const selectChildSemantic = queryAssertElement<HTMLDivElement>("#child-semantics");
  for (const type of ItemTypes) {
    const option = document.createElement('option');
    option.textContent = type;
    option.value = type;
    selectItemType.appendChild(option.cloneNode(true));
    selectChildType.appendChild(option);
  }
  for (const [category, values] of Object.entries(Semantics)) {
    for (const semantic of values) {
      const option = document.createElement('option');
      option.textContent = `${semantic} (${category})`;
      option.value = semantic;
      selectItemSemantic.appendChild(option.cloneNode(true));
      selectChildSemantic.appendChild(option);
    }
  }
}
function registerStaticEvents() {
  // open new action form
  queryAssertElement("#add-action-btn").addEventListener('click', () => {
    state.actionOnEditOriginal = null;
    state.actionOnEdit = { id: "", emptyValue: "", placeholders: [], affectedSemantics: [], requiredTags: [], template: "", affectedTypes: [], value: "", read: false, silent: false };
    bindActionForm(state.actionOnEdit);
    queryAssertElement("#action-dialog").toggleAttribute('open');
  });
  const getAddPlaceholderFn = (isShared: boolean) => () => {
    state.placeholderOnEditIsShared = isShared;
    state.placeholderOnEditOriginal = null;
    state.placeholderOnEdit = { label: "", mappedValues: {}, values: [] };
    bindPlaceholderForm(state.placeholderOnEdit);
    togglePlaceholderForm();
  };
  // open new action placeholder form
  queryAssertElement("#add-placeholder-btn").addEventListener('click', getAddPlaceholderFn(false));
  // open new shared placeholder form
  queryAssertElement("#add-shared-placeholder-btn").addEventListener('click', getAddPlaceholderFn(true));
  // save placeholder
  const savePlaceholderBtn = queryAssertElement<HTMLButtonElement>("#save-placeholder-btn");
  queryAssertElement("#save-placeholder-btn").addEventListener('click', () => {
    if (state.actionOnEdit && state.placeholderOnEdit) {
      const placeholderDialog = queryAssertElement<HTMLDivElement>("#action-dialog");
      if (!state.placeholderOnEditIsShared) {
        state.actionOnEdit.placeholders = state.actionOnEdit.placeholders.filter(ph => ph !== state.placeholderOnEditOriginal);
        state.actionOnEdit.placeholders.push(state.placeholderOnEdit);
        state.placeholderOnEdit = null;
        state.placeholderOnEditOriginal = null;
        const placeholdersContainer = queryAssertElement<HTMLDivElement>('#action-placeholders-container', placeholderDialog);
        renderPlaceholders(placeholdersContainer, state.actionOnEdit.placeholders, false);
        togglePlaceholderForm();
        onActionFormChange();
      } else {
        if (!state.placeholderOnEdit) {
          throw new Error("Application error: missing placeholder data");
        }
        savePlaceholderBtn.disabled = true;
        const placeholderPromise = state.placeholderOnEditOriginal ? httpAPI.updatePlaceholder(state.placeholderOnEditOriginal.label, state.placeholderOnEdit) : httpAPI.createPlaceholder(state.placeholderOnEdit);
        placeholderPromise
          .then(placeholder => {
            state.sharedPlaceholders = state.sharedPlaceholders.filter(ph => ph !== state.placeholderOnEditOriginal);
            state.sharedPlaceholders.push(placeholder);
            state.placeholderOnEdit = null;
            state.placeholderOnEditOriginal = null;
            const sharedPlaceholdersContainer = queryAssertElement<HTMLDivElement>('#shared-placeholders-container', placeholderDialog);
            renderPlaceholders(sharedPlaceholdersContainer, state.sharedPlaceholders, true);
            togglePlaceholderForm();
            onActionFormChange();
          }).catch(err => {
            console.error(err);
          }).finally(() => {
            savePlaceholderBtn.disabled = false;
          });

      }
    }
  });
  // remove placeholder
  queryAssertElement<HTMLDivElement>("#remove-placeholder-btn").addEventListener('click', () => {
    if (state.actionOnEdit && state.placeholderOnEdit) {
      if (!state.placeholderOnEditIsShared) {
        state.actionOnEdit.placeholders = state.actionOnEdit.placeholders.filter(ph => ph !== state.placeholderOnEditOriginal);
        const placeholdersContainer = queryAssertElement<HTMLDivElement>('#action-placeholders-container', queryAssertElement<HTMLDivElement>("#action-dialog"));
        renderPlaceholders(placeholdersContainer, state.actionOnEdit.placeholders, false);
        closePlaceholderForm();
        onActionFormChange();
      } else {
        const placeholderOnEditOriginal = state.placeholderOnEditOriginal;
        if (placeholderOnEditOriginal) {
          httpAPI.deletePlaceholder(placeholderOnEditOriginal.label)
            .then(() => {
              state.sharedPlaceholders = state.sharedPlaceholders.filter(ph => ph !== placeholderOnEditOriginal);
              const sharedPlaceholdersContainer = queryAssertElement<HTMLDivElement>('#shared-placeholders-container', queryAssertElement<HTMLDivElement>("#action-dialog"));
              renderPlaceholders(sharedPlaceholdersContainer, state.sharedPlaceholders, true);
              closePlaceholderForm();
              onActionFormChange();
            }).catch(err => {
              console.error(err);
            }).finally(() => {
              saveActionBtn.disabled = false;
            });
        }
      }
    }
  });
  // cancel edit/create placeholder
  queryAssertElement("#close-placeholder-form").addEventListener('click', closePlaceholderForm);
  // save action
  const saveActionBtn = queryAssertElement<HTMLButtonElement>("#save-action-btn");
  saveActionBtn.addEventListener('click', () => {
    if (state.actionOnEdit) {
      saveActionBtn.disabled = true;
      const actionPromise = state.actionOnEditOriginal ? httpAPI.updateAction(state.actionOnEditOriginal.id, state.actionOnEdit) : httpAPI.createAction(state.actionOnEdit);
      actionPromise.then(action => {
        // replace or add action list element
        let prevEl = undefined as HTMLElement | undefined;
        if (state.actionOnEditOriginal) {
          prevEl = state.actionElementMap.get(state.actionOnEditOriginal);
          state.actionElementMap.delete(state.actionOnEditOriginal);
        }
        const el = renderActionArticle(action);
        state.actionElementMap.set(action, el);
        const actionsContainer = queryAssertElement<HTMLDivElement>("#actions-container");
        if (prevEl) {
          actionsContainer.replaceChild(el, prevEl);
        } else {
          actionsContainer.appendChild(el);
        }
        closeActionForm();
      }).catch(err => {
        console.error(err);
      }).finally(() => {
        saveActionBtn.disabled = false;
      });
    }
  });
  // remove action
  queryAssertElement("#remove-action-btn").addEventListener('click', () => {
    const actionOnEditOriginal = state.actionOnEditOriginal;
    if (state.actionOnEdit && actionOnEditOriginal) {
      httpAPI.deleteAction(actionOnEditOriginal.id)
        .then(() => {
          // remove action list element
          const prevEl = state.actionElementMap.get(actionOnEditOriginal);
          state.actionElementMap.delete(actionOnEditOriginal);
          const actionsContainer = queryAssertElement<HTMLDivElement>("#actions-container");
          if (prevEl) actionsContainer.removeChild(prevEl);
          closeActionForm();
        }).catch(err => {
          console.error(err);
        }).finally(() => {
          saveActionBtn.disabled = false;
        });
    }
  });
  // cancel edit/create action
  queryAssertElement("#close-action-form").addEventListener('click', closeActionForm);
  // open test dialog
  queryAssertElement("#test-action-btn").addEventListener('click', toggleTestPanel);
  // close test dialog
  queryAssertElement("#close-test-dialog").addEventListener('click', toggleTestPanel);
  // run test
  queryAssertElement("#test-btn").addEventListener('click', async () => {
    const testDialog = queryAssertElement("#test-dialog");
    const testResultDialog = queryAssertElement("#test-result-dialog");
    const text = queryAssertElement<HTMLInputElement>("#test-input", testDialog).value ?? '';
    const dryRun = queryAssertElement<HTMLInputElement>("#dry-run", testDialog).checked ?? true;
    if (!text.length) {
      return;
    }
    let response = "---";
    let itemName = "---";
    let matchedTemplate = "---";
    try {
      const result = await httpAPI.test(text, dryRun);
      response = result.response;
      if (result.itemName.length) itemName = result.itemName;
      if (result.actionTemplate.length) matchedTemplate = result.actionTemplate;
    } catch (error) {
      console.error(error);
      response = (error as any).message ?? 'Error';
    }
    queryAssertElement('#test-response-text', testResultDialog).textContent = response;
    queryAssertElement('#test-item-text', testResultDialog).textContent = itemName;
    queryAssertElement('#test-template-text', testResultDialog).textContent = matchedTemplate;
    toggleTestResultPanel();
  });
  // close test result dialog
  queryAssertElement("#close-test-result-dialog").addEventListener('click', toggleTestResultPanel);
}
function toggleTestPanel() {
  queryAssertElement("#test-dialog").toggleAttribute('open');
}
function toggleTestResultPanel() {
  queryAssertElement("#test-result-dialog").toggleAttribute('open');
}
function queryAssertElement<T extends Element = Element>(query: string, parent: ParentNode = document) {
  const el = parent.querySelector<T>(query);
  if (!el) {
    throw new Error(`Missing element for query '${query}'; Please report the issue.`)
  }
  return el;
}
function closePlaceholderForm() {
  state.placeholderOnEdit = null;
  state.placeholderOnEditOriginal = null;
  togglePlaceholderForm();
}
function closeActionForm() {
  state.actionOnEdit = null;
  state.actionOnEditOriginal = null;
  toggleActionForm();
}

function toggleActionForm() {
  queryAssertElement("#action-dialog").toggleAttribute('open');
}
function togglePlaceholderForm() {
  queryAssertElement("#placeholder-dialog").toggleAttribute('open');
}
function bindPlaceholderForm(placeholder: ActionTemplatePlaceholder) {
  // handle show remove button
  const removeBtn = queryAssertElement("#remove-placeholder-btn");
  hideElement(removeBtn, state.placeholderOnEditOriginal == null);
  const formContainer = queryAssertElement<HTMLDivElement>("#placeholder-dialog");
  // handle label input
  const labelInput = recreateElement(queryAssertElement<HTMLInputElement>('#placeholder-label'));
  labelInput.value = placeholder.label;
  labelInput.addEventListener('input', function () {
    placeholder.label = this.value
    onPlaceholderFormChange();
  });
  // handle values list
  const valuesContainer = queryAssertElement<HTMLDivElement>('#values-container', formContainer);
  const _addValue = (value: string) => {
    const el = renderValue(value, () => {
      placeholder.values = placeholder.values.filter(v => v !== value);
      el.remove();
      onPlaceholderFormChange();
    });
    valuesContainer.appendChild(el);
    clearContent(valuesContainer);
    placeholder.values.forEach(_addValue);
    const valueInput = recreateElement(queryAssertElement<HTMLButtonElement>('#add-value-input', formContainer));
    const addValueBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-value', formContainer));
    valueInput.value = "";
    const _enableButton = () => {
      addValueBtn.disabled = !valueInput.value.trim().length;
    }
    _enableButton();
    valueInput.addEventListener('input', _enableButton);
    addValueBtn.addEventListener('click', () => {
      const value = valueInput.value.trim();
      if (value.length) {
        valueInput.value = "";
        placeholder.values.push(value);
        _addValue(value);
        onPlaceholderFormChange();
      }
    });
  }
  // handle mapped values list
  const mappedValuesContainer = queryAssertElement<HTMLDivElement>('#mapped-values-container', formContainer);
  const _addMappedValue = (text: string, value: string) => {
    const el = renderValue(`${text}=${value}`, () => {
      delete placeholder.mappedValues[text];
      el.remove();
    });
    mappedValuesContainer.appendChild(el);
  }
  clearContent(mappedValuesContainer);
  Object.entries(placeholder.mappedValues).forEach(([key, value]) => _addMappedValue(key, value));
  const tokensInput = recreateElement(queryAssertElement<HTMLButtonElement>('#add-mapped-text-input', formContainer));
  const valueInput = recreateElement(queryAssertElement<HTMLButtonElement>('#add-mapped-value-input', formContainer));
  const addMappedValueBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-mapped-value', formContainer));
  tokensInput.value = "";
  valueInput.value = "";
  const enableAddMappedValueButton = () => {
    addMappedValueBtn.disabled = !(!!tokensInput.value.trim().length &&
      !!valueInput.value.trim().length)
  }
  enableAddMappedValueButton();
  tokensInput.addEventListener('input', enableAddMappedValueButton);
  valueInput.addEventListener('input', enableAddMappedValueButton);
  addMappedValueBtn.addEventListener('click', () => {
    const text = tokensInput.value.trim();
    const value = valueInput.value.trim();
    if (text.length && value.length) {
      tokensInput.value = "";
      valueInput.value = "";
      placeholder.mappedValues[text] = value;
      _addMappedValue(text, value);
      onPlaceholderFormChange();
    }
  });
  // Force initial state check
  onPlaceholderFormChange();
}
function onActionFormChange() {
  const action = state.actionOnEdit;
  if (!action) return;
  const groupOptionsContainer = queryAssertElement<HTMLDivElement>("#group-options");
  if (action.affectedTypes.includes('Group')) {
    hideElement(groupOptionsContainer, false);
    if (!action.groupTargets) action.groupTargets = { affectedTypes: [], affectedSemantics: [], requiredTags: [], mergeState: false, recursive: false };
  } else {
    hideElement(groupOptionsContainer, true);
    if (action.groupTargets) delete action.groupTargets
  }
  const btn = queryAssertElement<HTMLButtonElement>("#save-action-btn");
  if (!btn) return;
  const error = validateActionForm();
  btn.disabled = !!error;
  btn.textContent = error ?? (state.actionOnEditOriginal ? "Update" : "Create");
}

function validateActionForm() {
  const action = state.actionOnEdit;
  if (!action) {
    return "Invalid state";
  }
  if (!action.template.length) {
    return "Template required";
  }
  const alternatives = action.template.split(';').map(a => a.trim());

  for (const alternative of alternatives) {
    const templateTokens = alternative.split(' ').map(t => t.trim()).flatMap(t => t.split('|')).map(t => {
      if (t.endsWith('?')) {
        return t.substring(0, t.length - 1);
      }
      return t;
    });
    const placeholderLabels = [] as string[];
    let hasItemLabel = false;
    for (const token of templateTokens) {
      if (token.startsWith('$')) {
        const placeholderLabel = token.substring(1);
        if (!RESERVED_WRITE_PLACEHOLDERS.includes(placeholderLabel) && ![...action.placeholders, ...state.sharedPlaceholders].some(p => p.label === placeholderLabel)) {
          return `Unknown placeholder ${token}`;
        }
        if (placeholderLabels.includes(placeholderLabel)) {
          return `Duplicated placeholder ${token}`;
        }
        placeholderLabels.push(placeholderLabel);
        if (ITEM_LABEL_PLACEHOLDER === placeholderLabel) {
          hasItemLabel = true;
        }
      }
    }
    if (!hasItemLabel) {
      return "Missing itemLabel";
    }
  }
  if (!action.value.length) {
    return "Value required";
  }
  if (!action.affectedTypes.length) {
    return "At least one type required";
  }
  return null;
}

function onPlaceholderFormChange() {
  const btn = queryAssertElement<HTMLButtonElement>("#save-placeholder-btn");
  const error = validatePlaceholderForm();
  btn.disabled = !!error;
  btn.textContent = error ?? (state.placeholderOnEditOriginal ? "Update" : "Create");
}

function validatePlaceholderForm() {
  const action = state.actionOnEdit;
  const placeholder = state.placeholderOnEdit;
  const isRead = state.actionOnEdit?.read ?? false;
  if (!action || !placeholder) {
    return "Missing data";
  }
  if (!placeholder.label.length) {
    return "Label is required";
  }
  if (!/^[a-z0-9]+$/i.test(placeholder.label)) {
    return "Label should be alphanumeric";
  }
  if (!(isRead && placeholder.label === STATE_PLACEHOLDER) && RESERVED_WRITE_PLACEHOLDERS.includes(placeholder.label)) {
    return "Label is reserved";
  }
  if ([...state.sharedPlaceholders, ...action.placeholders].some(p => p.label === placeholder.label)) {
    return "Placeholder already exists";
  }
  if (!placeholder.values.length && !Object.keys(placeholder.mappedValues).length) {
    return "At least one value is required";
  }
  return null;
}


function bindActionForm(config: ActionTemplateConfig) {
  const formContainer = queryAssertElement<HTMLDivElement>("#action-dialog");
  // handle show remove button
  const removeActionBtn = queryAssertElement("#remove-action-btn");
  hideElement(removeActionBtn, state.actionOnEditOriginal == null);
  // handle read switch
  const writeActionSection = recreateElement(queryAssertElement<HTMLInputElement>('#write-action-section', formContainer));
  const readActionSection = recreateElement(queryAssertElement<HTMLInputElement>('#read-action-section', formContainer));
  const toggleReadWriteOptions = (read: boolean) => {
    hideElement(writeActionSection, read);
    hideElement(readActionSection, !read);
  }
  toggleReadWriteOptions(config.read);
  const readSwitch = recreateElement(queryAssertElement<HTMLInputElement>('#action-read', formContainer));
  readSwitch.checked = config.read ?? false;
  readSwitch.addEventListener('input', function () {
    config.read = this.checked;
    toggleReadWriteOptions(config.read);
    onActionFormChange();
  });
  // handle action template
  const templateInput = recreateElement(queryAssertElement<HTMLInputElement>('#action-template-input', formContainer));
  templateInput.value = config.template;
  templateInput.addEventListener('input', function () {
    config.template = this.value
    onActionFormChange();
  });
  // handle action value
  const valueInput = recreateElement(queryAssertElement<HTMLInputElement>('#action-value', formContainer));
  valueInput.value = config.value;
  valueInput.addEventListener('input', function () {
    config.value = this.value
    onActionFormChange();
  });
  // handle action empty value
  const emptyValueInput = recreateElement(queryAssertElement<HTMLInputElement>('#action-empty-value', formContainer));
  emptyValueInput.value = config.emptyValue;
  emptyValueInput.addEventListener('input', function () {
    config.emptyValue = this.value;
    onActionFormChange();
  });
  // render placeholders
  const placeholdersContainer = queryAssertElement<HTMLDivElement>('#action-placeholders-container', formContainer);
  renderPlaceholders(placeholdersContainer, config.placeholders, false);
  // render shared placeholders
  const sharedPlaceholdersContainer = queryAssertElement<HTMLDivElement>('#shared-placeholders-container', formContainer);
  renderPlaceholders(sharedPlaceholdersContainer, state.sharedPlaceholders, true);
  // handle types
  const affectedTypesContainer = queryAssertElement<HTMLDivElement>('#affected-types-container', formContainer);
  // render types
  const _renderType = (type: string): void => {
    const el = renderValue(type, () => {
      config.affectedTypes = config.affectedTypes.filter(t => t !== type);
      el.remove();
      onActionFormChange();
    });
    affectedTypesContainer.appendChild(el);
  };
  clearContent(affectedTypesContainer);
  config.affectedTypes.forEach(_renderType);
  // handle add button
  const addTypeBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-type-btn', formContainer));
  const typesSelect = queryAssertElement<HTMLSelectElement>('#available-types', formContainer);
  if (addTypeBtn && typesSelect) {
    addTypeBtn.addEventListener('click', () => {
      const type = typesSelect.value;
      if (type.length && !config.affectedTypes.includes(type)) {
        config.affectedTypes.push(type);
        _renderType(type);
        onActionFormChange();
      }
    });
  }
  // handle semantics
  const affectedSemanticsContainer = queryAssertElement<HTMLDivElement>('#affected-semantics-container', formContainer);
  // render semantics
  const _renderSemantic = (semantic: string): void => {
    const el = renderValue(semantic, () => {
      el.remove();
      config.affectedSemantics = config.affectedSemantics.filter(s => s !== semantic);
      onActionFormChange();
    });
    affectedSemanticsContainer.appendChild(el);
  };
  clearContent(affectedSemanticsContainer);
  config.affectedSemantics.forEach(_renderSemantic);
  // handle add button
  const addSemanticBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-semantic-btn', formContainer));
  const semanticsSelect = queryAssertElement<HTMLSelectElement>('#available-semantics', formContainer);
  if (addSemanticBtn && semanticsSelect) {
    addSemanticBtn.addEventListener('click', () => {
      const semantic = semanticsSelect.value;
      if (semantic.length && !config.affectedSemantics.includes(semantic)) {
        config.affectedSemantics.push(semantic);
        _renderSemantic(semantic);
        onActionFormChange();
      }
    });
  }
  // handle tags
  const requiredTagsContainer = queryAssertElement<HTMLDivElement>('#required-tags-container', formContainer);
  // render tags
  const _renderTag = (tag: string): void => {
    const el = renderValue(tag, () => {
      el.remove();
      config.requiredTags = config.requiredTags.filter(s => s !== tag);
      onActionFormChange();
    });
    requiredTagsContainer.appendChild(el);
  };
  clearContent(requiredTagsContainer);
  config.requiredTags.forEach(_renderTag);
  // handle add button
  const addTagBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-tag-btn', formContainer));
  const tagInput = recreateElement(queryAssertElement<HTMLInputElement>('#tag-input', formContainer));
  if (addTagBtn && tagInput) {
    tagInput.value = "";
    const _enableButton = () => {
      addTagBtn.disabled = !tagInput.value.trim().length;
    }
    _enableButton();
    tagInput.addEventListener('input', _enableButton);
    addTagBtn.addEventListener('click', () => {
      const tag = tagInput.value.trim();
      tagInput.value = "";
      if (tag.length && !config.requiredTags.includes(tag)) {
        config.requiredTags.push(tag);
        _renderTag(tag);
        onActionFormChange();
      }
    });
  }
  // handle silent switch
  const silentSwitch = recreateElement(queryAssertElement<HTMLInputElement>('#action-silent', formContainer));
  silentSwitch.checked = config.silent ?? false;
  silentSwitch.addEventListener('input', function () {
    config.silent = this.checked;
    onActionFormChange();
  });
  // handle group targets types
  const childTypesContainer = queryAssertElement<HTMLDivElement>('#child-types-container', formContainer);
  if (childTypesContainer) {
    // render types
    const _renderType = (type: string): void => {
      const el = renderValue(type, () => {
        if (!config.groupTargets) return;
        config.groupTargets.affectedTypes = config.groupTargets.affectedTypes.filter(t => t !== type);
        el.remove();
        onActionFormChange();
      });
      childTypesContainer.appendChild(el);
    };
    clearContent(childTypesContainer);
    config.groupTargets?.affectedTypes.forEach(_renderType);
    // handle add button
    const addTypeBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-child-type-btn', formContainer));
    const typesSelect = queryAssertElement<HTMLSelectElement>('#child-types', formContainer);
    if (addTypeBtn && typesSelect) {
      addTypeBtn.addEventListener('click', () => {
        if (!config.groupTargets) return;
        const type = typesSelect.value;
        if (type.length && !config.groupTargets.affectedTypes.includes(type)) {
          config.groupTargets.affectedTypes.push(type);
          _renderType(type);
          onActionFormChange();
        }
      });
    }
  }
  // handle group targets semantics
  const childSemanticsContainer = queryAssertElement<HTMLDivElement>('#child-semantics-container', formContainer);
  if (childSemanticsContainer) {
    // render semantics
    const _renderSemantic = (semantic: string): void => {
      const el = renderValue(semantic, () => {
        if (!config.groupTargets) return;
        el.remove();
        config.groupTargets.affectedSemantics = config.groupTargets?.affectedSemantics.filter(s => s !== semantic);
        onActionFormChange();
      });
      childSemanticsContainer.appendChild(el);
    };
    clearContent(childSemanticsContainer);
    config.groupTargets?.affectedSemantics.forEach(_renderSemantic);
    // handle add button
    const addSemanticBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-child-semantic-btn', formContainer));
    const semanticsSelect = queryAssertElement<HTMLSelectElement>('#child-semantics', formContainer);
    if (addSemanticBtn && semanticsSelect) {
      addSemanticBtn.addEventListener('click', () => {
        if (!config.groupTargets) return;
        const semantic = semanticsSelect.value;
        if (semantic.length && !config.groupTargets.affectedSemantics.includes(semantic)) {
          config.groupTargets.affectedSemantics.push(semantic);
          _renderSemantic(semantic);
          onActionFormChange();
        }
      });
    }
    // handle group targets recursive
    const recursiveSwitch = recreateElement(queryAssertElement<HTMLInputElement>('#action-recursive', formContainer));
    recursiveSwitch.checked = config.groupTargets?.recursive ?? false;
    recursiveSwitch.addEventListener('input', function () {
      if (!config.groupTargets) return;
      config.groupTargets.recursive = this.checked;
      onActionFormChange();
    });
    // handle group targets merge state
    const mergeStateSwitch = recreateElement(queryAssertElement<HTMLInputElement>('#action-merge-state', formContainer));
    mergeStateSwitch.checked = config.groupTargets?.mergeState ?? false;
    mergeStateSwitch.addEventListener('input', function () {
      if (!config.groupTargets) return;
      config.groupTargets.mergeState = this.checked;
      onActionFormChange();
    });
  }

  // handle group targets tags
  const requiredChildTagsContainer = queryAssertElement<HTMLDivElement>('#required-child-tags-container', formContainer);
  if (requiredChildTagsContainer) {
    // render tags
    const _renderTag = (tag: string): void => {
      const el = renderValue(tag, () => {
        if (!config.groupTargets) return;
        el.remove();
        config.groupTargets.requiredTags = config.groupTargets.requiredTags.filter(s => s !== tag);
        onActionFormChange();
      });
      requiredChildTagsContainer.appendChild(el);
    };
    clearContent(requiredChildTagsContainer);
    config.groupTargets?.requiredTags.forEach(_renderTag);
    // handle add button
    const addTagBtn = recreateElement(queryAssertElement<HTMLButtonElement>('#add-child-tag-btn', formContainer));
    const tagInput = recreateElement(queryAssertElement<HTMLInputElement>('#child-tag-input', formContainer));
    if (addTagBtn && tagInput) {
      tagInput.value = "";
      const _enableButton = () => {
        addTagBtn.disabled = !tagInput.value.trim().length;
      }
      _enableButton();
      tagInput.addEventListener('input', _enableButton);
      addTagBtn.addEventListener('click', () => {
        if (!config.groupTargets) return;
        const tag = tagInput.value.trim();
        tagInput.value = "";
        if (tag.length && !config.groupTargets.requiredTags.includes(tag)) {
          config.groupTargets.requiredTags.push(tag);
          _renderTag(tag);
          onActionFormChange();
        }
      });
    }
  }
  onActionFormChange();
}

function hideElement(element: Element, enabled: boolean) {
  if (enabled) {
    element.classList.add('hidden');
  } else {
    element.classList.remove('hidden');
  }
}

function renderActionArticle(action: ActionTemplateConfig) {
  // clone the article template content
  const actionArticleTemplate = queryAssertElement<HTMLTemplateElement>("#action-article-template");
  const actionArticle = queryAssertElement<HTMLElement>('article', actionArticleTemplate.content.cloneNode(true) as ParentNode);
  // set read/write
  queryAssertElement("#action-type", actionArticle).textContent = action.read ? "Read" : "Write";
  // Render action types/semantics/tags
  const actionFilter = queryAssertElement<HTMLDivElement>("#action-filter", actionArticle);
  clearContent(actionFilter);
  const _render = (t: string) => {
    const p = renderValue(t);
    actionFilter.appendChild(p)
  };
  action.affectedTypes.forEach(_render);
  action.affectedSemantics.forEach(_render);
  action.requiredTags.forEach(_render);
  // Render action template alternatives
  const actionTemplateContainer = queryAssertElement<HTMLDivElement>("#action-template-container", actionArticle);
  clearContent(actionTemplateContainer);
  const alternatives = getTemplateTokenAlternatives(action);
  const allPlaceholders = [...action.placeholders, ...state.sharedPlaceholders];
  for (const alternativeTokens of alternatives) {
    const tokensEl = renderTokens(alternativeTokens, allPlaceholders);
    actionTemplateContainer.appendChild(tokensEl);
  }
  // Handle edit btn
  queryAssertElement('#edit-btn', actionArticle).addEventListener('click', () => {
    state.actionOnEditOriginal = action;
    state.actionOnEdit = {
      id: action.id,
      emptyValue: action.emptyValue,
      placeholders: action.placeholders.map(ph => ({ label: ph.label, values: [...ph.values], mappedValues: { ...ph.mappedValues } })),
      affectedSemantics: [...action.affectedSemantics],
      requiredTags: [...action.requiredTags],
      template: action.template,
      affectedTypes: [...action.affectedTypes],
      value: action.value,
      read: action.read,
      silent: action.silent,
      groupTargets: action.groupTargets == null ? undefined : ({
        affectedTypes: [...action.groupTargets.affectedTypes],
        affectedSemantics: [...action.groupTargets.affectedSemantics],
        requiredTags: [...action.groupTargets.requiredTags],
        mergeState: action.groupTargets.mergeState,
        recursive: action.groupTargets.recursive,
      }),
    };
    bindActionForm(state.actionOnEdit);
    toggleActionForm();
  });
  return actionArticle;
}

function getTemplateTokenAlternatives(action: ActionTemplateConfig): string[][] {
  let output = [] as string[][];
  const alternatives = action.template.split(';');
  for (const alternative of alternatives) {
    let altOutput = [[]] as string[][];
    const optTokens = alternative.split(' ').filter(t => t.length > 0);
    for (const optToken of optTokens) {
      const tokens = optToken.split('|');
      const isOptional = tokens.some(t => t.endsWith('?'));
      let newOutput = !isOptional ? [] : altOutput.map(a => [...a]);
      for (let token of tokens) {
        if (token.endsWith('?')) {
          token = token.substring(0, token.length - 1);
        }
        newOutput.push(...altOutput.map(a => [...a, token]));
      }
      altOutput = newOutput;
    }
    output.push(...altOutput.filter(a => a.length > 0));
  }
  return output;
}

function renderTokens(tokens: string[], placeholders: ActionTemplatePlaceholder[]): HTMLElement {
  const block = document.createElement('h4');
  const _renderPlaceholder = (text: string, desc: string) => {
    const el = document.createElement('abbr');
    el.setAttribute('data-tooltip', desc);
    el.textContent = `${text}`;
    return el;
  }
  const _renderToken = (text: string) => {
    const el = document.createElement('span');
    el.textContent = `${text}`;
    return el;
  }
  for (var i = 0; i < tokens.length; i++) {
    const token = tokens[i];
    if (token.startsWith('$')) {
      const label = token.substring(1);
      switch (label) {
        case ITEM_LABEL_PLACEHOLDER:
          block.appendChild(_renderPlaceholder(token, "One of your item labels or synonyms."));
          break;
        case DYNAMIC_PLACEHOLDER:
          block.appendChild(_renderPlaceholder(token, "Match any tokens."));
          break;
        case GROUP_LABEL_PLACEHOLDER:
          block.appendChild(_renderPlaceholder(token, "Parent group label."));
          break;
        case ITEM_OPTION_PLACEHOLDER:
          block.appendChild(_renderPlaceholder(token, "Takes values from item StateDescription and CommandOptions."));
          break;
        case STATE_PLACEHOLDER:
          const statePh = placeholders.find(p => p.label === label);
          let stateMaps = [] as string[];
          Object.entries(statePh?.mappedValues ?? {}).forEach(([tokens, value]) => `${tokens}=${value}`);
          block.appendChild(_renderPlaceholder(token, "The item value." + (stateMaps.length ? ` With transformations: ${stateMaps.join(', ')}` : '')));
          break;
        default:
          const values = [] as string[];
          const ph = placeholders.find(p => p.label === label);
          if (ph) {
            values.push(...ph.values);
            values.push(...Object.entries(ph.mappedValues).map(([tokens, value]) => `${tokens}=${value}`));
          }
          block.appendChild(_renderPlaceholder(token, !values.length ? 'Unknown label.' : `Possible values: ${values.join(', ')}.`));
      }
    } else {
      block.appendChild(_renderToken(token))
    }
    block.appendChild(_renderToken(i == tokens.length - 1 ? '.' : ' '));
  }
  return block;
}

function renderPlaceholders(placeholdersContainer: HTMLDivElement, placeholders: ActionTemplatePlaceholder[], isShared: boolean) {
  clearContent(placeholdersContainer);
  placeholders.forEach(ph => {
    const el = renderValue(ph.label, () => {
      state.placeholderOnEdit = { label: ph.label, values: [...ph.values], mappedValues: { ...ph.mappedValues } }
      state.placeholderOnEditOriginal = ph;
      state.placeholderOnEditIsShared = isShared;
      bindPlaceholderForm(state.placeholderOnEdit);
      togglePlaceholderForm();
      onActionFormChange();
    });
    placeholdersContainer.appendChild(el);
  });
}

/**
 * recreate element to remove event listeners
 * @param el nullable element ref
 * @returns cloned element if any
 */
function recreateElement<T extends Element>(el: T): T;
function recreateElement<T extends Element>(el: T | null): T | null;
function recreateElement<T extends Element>(el: T | null): T | null {
  if (!el?.parentNode) {
    return el;
  }
  const clone = el.cloneNode(true) as T;
  el.parentNode.replaceChild(clone, el);
  return clone;
}

function clearContent(div: HTMLDivElement) {
  while (div.lastElementChild) div.removeChild(div.lastElementChild);
}

function renderValue(value: string, onClick?: () => void) {
  const p = document.createElement('p');
  const kbd = document.createElement('kbd');
  kbd.textContent = value;
  if (onClick) {
    kbd.addEventListener('click', onClick);
  }
  p.appendChild(kbd);
  return p;
}