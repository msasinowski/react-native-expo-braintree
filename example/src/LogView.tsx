import * as React from 'react';
import {
  ActivityIndicator,
  Clipboard,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';

export interface LogState {
  loading: boolean;
  result: string | null;
  error: string | null;
}

export const LogView = ({
  state,
  onClear,
}: {
  state: LogState;
  onClear: () => void;
}) => {
  const copyToClipboard = () => {
    const textToCopy = state.error || state.result;
    if (textToCopy) Clipboard.setString(textToCopy);
  };

  return (
    <View style={styles.resultBox}>
      <View style={styles.resultHeaderContainer}>
        <Text style={styles.resultHeader}>Output Logs</Text>
        <View style={styles.logActions}>
          {(state.result || state.error) && (
            <>
              <TouchableOpacity
                onPress={copyToClipboard}
                style={styles.actionButton}
              >
                <Text style={styles.actionText}>Copy</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={onClear}>
                <Text style={styles.actionText}>Clear</Text>
              </TouchableOpacity>
            </>
          )}
        </View>
      </View>
      {state.loading ? (
        <ActivityIndicator color="#0070ba" size="small" />
      ) : (
        <ScrollView nestedScrollEnabled style={styles.resultScroll}>
          {state.error && <Text style={styles.errorText}>{state.error}</Text>}
          {state.result && (
            <Text style={styles.resultText}>{state.result}</Text>
          )}
        </ScrollView>
      )}
    </View>
  );
};
const styles = StyleSheet.create({
  resultBox: {
    marginTop: 10,
    padding: 10,
    backgroundColor: '#f9f9f9',
    borderRadius: 8,
    minHeight: 180,
    maxHeight: 180,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  resultHeaderContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 5,
  },
  resultHeader: {
    fontSize: 9,
    fontWeight: 'bold',
    color: '#aaa',
    textTransform: 'uppercase',
  },
  clearText: { fontSize: 10, color: '#0070ba' },
  resultScroll: { flex: 1 },
  resultText: { fontFamily: 'monospace', fontSize: 10, color: '#333' },
  errorText: { color: '#d32f2f', fontSize: 10 },
  logActions: { flexDirection: 'row' },
  actionButton: { marginRight: 15 },
  actionText: { fontSize: 10, color: '#0070ba', fontWeight: 'bold' },
});
