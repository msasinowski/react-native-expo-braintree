export const tryParseJson = (value: string): unknown => {
  try {
    return JSON.parse(value);
  } catch {
    return value;
  }
};

export const parseKnownFields = (
  result: Record<string, string>,
  nestedFields: string[]
): Record<string, unknown> => {
  const out: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(result)) {
    out[key] = nestedFields.includes(key) ? tryParseJson(value) : value;
  }
  return out;
};

export const handleResult = <T>(
  result: unknown,
  nestedFields?: string[]
): T => {
  if (
    result &&
    typeof result === 'object' &&
    result &&
    'error' in result &&
    result.error === 'true'
  ) {
    throw result;
  }
  if (nestedFields && result && typeof result === 'object') {
    return parseKnownFields(
      result as Record<string, string>,
      nestedFields
    ) as T;
  }
  return result as T;
};

export const catchError = <T>(ex: unknown): T => {
  return ex as T;
};
